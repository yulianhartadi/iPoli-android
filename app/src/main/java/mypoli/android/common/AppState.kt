package mypoli.android.common

import mypoli.android.common.redux.Action
import mypoli.android.common.redux.Reducer
import mypoli.android.common.redux.State
import mypoli.android.player.Player
import mypoli.android.quest.calendar.CalendarAction
import mypoli.android.quest.calendar.CalendarReducer
import mypoli.android.quest.calendar.CalendarState

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/23/2018.
 */

sealed class LoadDataAction : Action {
    object All : LoadDataAction()
}

sealed class DataLoadedAction : Action {
    data class PlayerLoaded(val player: Player) : DataLoadedAction()
}

data class AppState(
    val calendarState: CalendarState
) : State

object AppReducer : Reducer<AppState, Action> {

    override fun reduce(state: AppState, action: Action) =
        when (action) {
            is CalendarAction -> state.copy(
                calendarState = CalendarReducer.reduce(state.calendarState, action)
            )
            else -> state
        }

    override fun defaultState() =
        AppState(
            calendarState = CalendarReducer.defaultState()
        )
}