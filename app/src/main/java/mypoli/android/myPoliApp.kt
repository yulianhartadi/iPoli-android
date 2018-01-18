package mypoli.android

import android.app.Application
import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.evernote.android.job.JobManager
import com.github.moduth.blockcanary.BlockCanary
import com.github.moduth.blockcanary.BlockCanaryContext
import com.jakewharton.threetenabp.AndroidThreeTen
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import io.fabric.sdk.android.Fabric
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.common.di.*
import mypoli.android.common.job.myPoliJobCreator
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository
import mypoli.android.quest.calendar.CalendarPresenter
import mypoli.android.quest.calendar.CalendarViewState
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import space.traversal.kapsule.transitive
import timber.log.Timber


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 7/7/17.
 */

class myPoliApp : Application() {

    private lateinit var module: Module

    companion object {
        lateinit var refWatcher: RefWatcher

        fun module(context: Context) =
            (context.applicationContext as myPoliApp).module
    }

    override fun onCreate() {
        super.onCreate()


        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }


        AndroidThreeTen.init(this)
        // Initialize Realm. Should only be done once when the application starts.
//        Realm.init(this)
//        val db = Database()
        Timber.plant(Timber.DebugTree())


//        Logger.addLogAdapter(AndroidLogAdapter())

//        Timber.plant(object : Timber.DebugTree() {
//            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//                Logger.log(priority, tag, message, t)
//            }
//        })

        if (!BuildConfig.DEBUG) {

            BlockCanary.install(this, object : BlockCanaryContext() {
                override fun provideBlockThreshold(): Int {
                    return 500
                }
            }).start()

            Fabric.with(
                Fabric.Builder(this)
                    .kits(Crashlytics())
                    .debuggable(BuildConfig.DEBUG)
                    .build()
            )

            refWatcher = LeakCanary.install(this)
        }

        JobManager.create(this).addJobCreator(myPoliJobCreator())

        val currentUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler({ thread, exception ->
            Log.println(Log.ERROR, thread.name, Log.getStackTraceString(exception))
            currentUncaughtExceptionHandler.uncaughtException(thread, exception)
        })


        module = Module(
            androidModule = MainAndroidModule(this),
            repositoryModule = CouchbaseRepositoryModule(),
            useCaseModule = MainUseCaseModule(),
            presenterModule = AndroidPresenterModule(),
            stateStoreModule = AndroidStateStoreModule()
        ).transitive()
    }
}

interface Action

sealed class PlayerAction : Action {
    object Load : PlayerAction()
    data class Changed(val player: Player) : PlayerAction()
}

sealed class CalendarAction : Action {
    object ExpandToolbar : CalendarAction()
}

interface State

interface PartialState

data class AppState(
    val player: Player? = null,
    val calendarState: CalendarState
) : State

data class CalendarState(
    val currentDate: LocalDate,
    val datePickerState: CalendarViewState.DatePickerState,
    val monthText: String,
    val dayText: String,
    val dateText: String,
    val adapterPosition: Int
) : PartialState

class LoadPlayerMiddleWare(private val playerRepository: PlayerRepository) : MiddleWare<AppState> {
    override fun execute(store: AppStateStore<AppState>, action: Action) {
        if (action != PlayerAction.Load) {
            return
        }
        launch {
            playerRepository.listen().consumeEach {
                launch(UI) {
                    store.dispatch(PlayerAction.Changed(it!!))
                }

            }
        }
    }
}

interface MiddleWare<in S : State> {
    fun execute(store: AppStateStore<S>, action: Action)
}

interface Reducer<S : State> {
    fun reduce(oldState: S, action: Action): S

    fun defaultState(): S
}

interface PartialReducer<in S : State, P : PartialState, in A : Action> {
    fun reduce(globalState: S, partialState: P, action: A): P

    fun defaultState(): P
}

object AppReducer : Reducer<AppState> {
    override fun defaultState(): AppState {
        return AppState(
            calendarState = CalendarReducer.defaultState()
        )
    }

    override fun reduce(oldState: AppState, action: Action): AppState {

        val player = if (action is PlayerAction.Changed) {
            action.player
        } else {
            oldState.player
        }

        val calendarState = if (action is CalendarAction) {
            CalendarReducer.reduce(
                oldState,
                oldState.calendarState,
                action
            )
        } else {
            oldState.calendarState
        }

        return oldState.copy(
            player = player,
            calendarState = calendarState
        )
    }
}

object CalendarReducer : PartialReducer<AppState, CalendarState, CalendarAction> {

    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM")

    override fun reduce(
        globalState: AppState,
        partialState: CalendarState,
        action: CalendarAction
    ) =
        when (action) {
            is CalendarAction.ExpandToolbar -> {
                partialState.copy(
                    datePickerState = CalendarViewState.DatePickerState.SHOW_WEEK
                )
            }
        }

    override fun defaultState(): CalendarState {
        val today = LocalDate.now()
        return CalendarState(
            currentDate = today,
            datePickerState = CalendarViewState.DatePickerState.INVISIBLE,
            adapterPosition = CalendarPresenter.MID_POSITION,
            monthText = monthFormatter.format(today),
            dateText = "",
            dayText = ""
        )
    }
}

interface StateChangeSubscriber<in S : State> {
    fun onStateChanged(newState: S)
}

class AppStateStore<out S : State>(
    initialState: S,
    private val reducer: Reducer<S>,
    private val middleWares: List<MiddleWare<S>> = listOf()
) {
    private var stateChangeSubscribers: List<StateChangeSubscriber<S>> = listOf()

    private var state = initialState

    fun dispatch(action: Action) {
        middleWares.forEach {
            it.execute(this, action)
        }
        val newState = reducer.reduce(state, action)
        if (newState != state) {
            state = newState
            stateChangeSubscribers.forEach {
                it.onStateChanged(state)
            }
        }
    }

    fun subscribe(subscriber: StateChangeSubscriber<S>) {
        stateChangeSubscribers += subscriber
        subscriber.onStateChanged(state)
    }

    fun unsubscribe(subscriber: StateChangeSubscriber<S>) {
        stateChangeSubscribers -= subscriber
    }
}