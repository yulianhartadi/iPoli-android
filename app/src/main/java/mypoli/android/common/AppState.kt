package mypoli.android.common

import mypoli.android.common.redux.Action
import mypoli.android.common.redux.Reducer
import mypoli.android.common.redux.State
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

data class AppState(
    val appDataState: AppDataState,
    val calendarState: CalendarState
) : State

object AppReducer : Reducer<AppState, Action> {

    override fun reduce(state: AppState, action: Action) =
        when (action) {
            is CalendarAction -> state.copy(
                calendarState = CalendarReducer.reduce(state.calendarState, action)
            )
            is DataLoadedAction -> state.copy(
                appDataState = AppDataReducer.reduce(state.appDataState, action)
            )
            else -> state
        }

    override fun defaultState() =
        AppState(
            appDataState = AppDataReducer.defaultState(),
            calendarState = CalendarReducer.defaultState()
        )
}