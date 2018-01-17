package mypoli.android

import android.app.Application
import android.content.Context
import android.util.Log
import com.bluelinelabs.conductor.Router
import com.crashlytics.android.Crashlytics
import com.evernote.android.job.JobManager
import com.github.moduth.blockcanary.BlockCanary
import com.github.moduth.blockcanary.BlockCanaryContext
import com.jakewharton.threetenabp.AndroidThreeTen
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import io.fabric.sdk.android.Fabric
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.common.di.*
import mypoli.android.common.job.myPoliJobCreator
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository
import space.traversal.kapsule.transitive
import timber.log.Timber


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 7/7/17.
 */

class myPoliApp : Application() {

    companion object {
        lateinit var refWatcher: RefWatcher

        fun controllerModule(context: Context, router: Router?) = ControllerModule(
            androidModule = MainAndroidModule(context),
            navigationModule = AndroidNavigationModule(router),
            repositoryModule = CouchbaseRepositoryModule(),
            useCaseModule = MainUseCaseModule(),
            presenterModule = AndroidPresenterModule()
        ).transitive()

        fun simpleModule(context: Context) = SimpleModule(
            androidModule = MainAndroidModule(context),
            repositoryModule = CouchbaseJobRepositoryModule(),
            useCaseModule = AndroidPopupUseCaseModule(),
            presenterModule = AndroidPopupPresenterModule()
        ).transitive()
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


        val stateStore =
            AppStateStore(AppState(), LoadPlayerMiddleWare(simpleModule(this).playerRepository))
        stateStore.dispatch(PlayerAction.Load)
    }
}

interface Action

sealed class PlayerAction : Action {
    object Load : PlayerAction()
    data class Changed(val player: Player) : PlayerAction()
}

data class AppState(val player: Player? = null)

class LoadPlayerMiddleWare(private val playerRepository: PlayerRepository) {

    suspend fun apply(store: AppStateStore, action: PlayerAction.Load) {
        playerRepository.listen().consumeEach {
            store.dispatch(PlayerAction.Changed(it!!))
        }
    }
}

object PlayerActionReducer {
    fun reduce(oldState: AppState, action: PlayerAction) =
        when (action) {
            is PlayerAction.Load -> {
                oldState
            }
            is PlayerAction.Changed -> {
                oldState.copy(player = action.player)
            }
        }
}

class AppStateStore(initialState: AppState, loadPlayerMiddleWare: LoadPlayerMiddleWare) {

    var state = initialState

    val reducers = mapOf(
        PlayerAction::class to PlayerActionReducer
    )

    val middleWare = mapOf(
        PlayerAction.Load::class to loadPlayerMiddleWare
    )

    inline fun <reified A : Action> dispatch(action: A) {
        for (actionClass in reducers.keys) {
//            if (actionClass is A) {
            val state = reducers[actionClass]!!.reduce(state, action as PlayerAction)
            onStateChanged(state)
//            }
        }

        for (actionClass in middleWare.keys) {
//            Timber.d("AAA action class $actionClass")
//            Timber.d("AAA A ${A::class}")
//            if (actionClass::class == A::class) {
            launch {
                middleWare[actionClass]?.apply(this@AppStateStore, action as PlayerAction.Load)
            }
//            }
        }
    }

    fun onStateChanged(newState: AppState) {
        Timber.d("AAA new state $newState")
    }
}