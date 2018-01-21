package mypoli.android.common.redux

import mypoli.android.player.Player
import mypoli.android.quest.calendar.CalendarViewState
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/20/2018.
 */

interface Action

interface AsyncAction : Action {
    suspend fun execute(dispatcher: Dispatcher)
}

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

object CalendarReducer : Reducer<CalendarState, CalendarAction> {
    override fun reduce(state: CalendarState, action: CalendarAction): CalendarState {
        return state
    }

    override fun defaultState() =
        CalendarState(
            LocalDate.now(),
            CalendarViewState.DatePickerState.INVISIBLE,
            "",
            "",
            "",
            -1
        )

}

object AppReducer : Reducer<AppState, Action> {

    override fun reduce(state: AppState, action: Action) =
        state.copy(
            calendarState = if (action is CalendarAction)
                CalendarReducer.reduce(state.calendarState, action)
            else
                state.calendarState
        )

    override fun defaultState() =
        AppState(
            calendarState = CalendarReducer.defaultState()
        )

}

data class CalendarState(
    val currentDate: LocalDate,
    val datePickerState: CalendarViewState.DatePickerState,
    val monthText: String,
    val dayText: String,
    val dateText: String,
    val adapterPosition: Int
) : State

//class LoadPlayerMiddleWare(private val playerRepository: PlayerRepository) : MiddleWare<AppState> {
//    override fun execute(store: StateStore<AppState>, action: Action) {
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

interface Reducer<S : State, in A : Action> {

    fun reduce(state: S, action: A): S

    fun defaultState(): S
}

//interface PartialReducer<in S : State, P : PartialState, in A : Action> {
//    fun reduce(globalState: S, partialState: P, action: A): P
//
//    fun defaultState(): P
//}
//
//object AppReducer : Reducer<AppState> {
//    override fun defaultState(): AppState {
//        return AppState(
//            calendarState = CalendarReducer.defaultState()
//        )
//    }
//
//    override fun reduce(oldState: AppState, action: Action): AppState {
//
//        val player = if (action is PlayerAction.Changed) {
//            action.player
//        } else {
//            oldState.player
//        }
//
//        val calendarState = if (action is CalendarAction) {
//            CalendarReducer.reduce(
//                oldState,
//                oldState.calendarState,
//                action
//            )
//        } else {
//            oldState.calendarState
//        }
//
//        return oldState.copy(
//            player = player,
//            calendarState = calendarState
//        )
//    }
//}
//
//object CalendarReducer : PartialReducer<AppState, CalendarState, CalendarAction> {
//
//    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM")
//
//    override fun reduce(
//        globalState: AppState,
//        partialState: CalendarState,
//        action: CalendarAction
//    ) =
//        when (action) {
//            is CalendarAction.ExpandToolbar -> {
//                partialState.copy(
//                    datePickerState = CalendarViewState.DatePickerState.SHOW_WEEK
//                )
//            }
//        }
//
//    override fun defaultState(): CalendarState {
//        val today = LocalDate.now()
//        return CalendarState(
//            currentDate = today,
//            datePickerState = CalendarViewState.DatePickerState.INVISIBLE,
//            adapterPosition = CalendarPresenter.MID_POSITION,
//            monthText = monthFormatter.format(today),
//            dateText = "",
//            dayText = ""
//        )
//    }
//}

interface StateChangeSubscriber<in S : State> {
    fun onStateChanged(newState: S)
}

interface Dispatcher {
    fun dispatch(action: Action)
}

class StateStore<out S : State>(
    private val reducer: Reducer<S, Action>,
    middleware: List<MiddleWare<S>> = listOf()
) : Dispatcher {

    private var stateChangeSubscribers: List<StateChangeSubscriber<S>> = listOf()
    private var state = reducer.defaultState()
    private val middleWare = CompositeMiddleware<S>(middleware)

    override fun dispatch(action: Action) {

        val res = middleWare.execute(state, this, action)
        if (res == MiddleWare.Result.Continue) {
            changeState(action)
        }
    }

    private fun changeState(action: Action) {
        val newState = reducer.reduce(state, action)
        val oldState = state
        state = newState
        notifyStateChanged(oldState, state)
    }

    private fun notifyStateChanged(oldState: S, newState: S) {
        stateChangeSubscribers.forEach {
            it.onStateChanged(newState)
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