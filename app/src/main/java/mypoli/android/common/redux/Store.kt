package mypoli.android.common.redux

import mypoli.android.player.Player
import mypoli.android.quest.calendar.CalendarPresenter
import mypoli.android.quest.calendar.CalendarViewState
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/20/2018.
 */

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

//class LoadPlayerMiddleWare(private val playerRepository: PlayerRepository) : MiddleWare<AppState> {
//    override fun execute(store: AppStateStore<AppState>, action: Action) {
//        if (action != PlayerAction.Load) {
//            return
//        }
//        launch {
//            playerRepository.listen().consumeEach {
//                launch(UI) {
//                    store.dispatch(PlayerAction.Changed(it!!))
//                }
//
//            }
//        }
//    }
//}

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

interface Dispatcher {
    fun dispatch(action: Action)
}

class AppStateStore<out S : State>(
    initialState: S,
    private val reducer: Reducer<S>,
    private val middleWares: List<MiddleWare<S>> = listOf()
) : Dispatcher {
    private var stateChangeSubscribers: List<StateChangeSubscriber<S>> = listOf()

    private var state = initialState

    override fun dispatch(action: Action) {
        middleWares.forEach {
            it.execute(state, this, action)
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