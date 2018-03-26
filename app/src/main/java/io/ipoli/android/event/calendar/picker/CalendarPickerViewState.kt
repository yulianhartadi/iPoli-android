package io.ipoli.android.event.calendar.picker

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.event.Calendar
import io.ipoli.android.pet.PetAvatar

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/11/2018.
 */

sealed class CalendarPickerAction : Action {
    object Load : CalendarPickerAction()
}

object CalendarPickerReducer : BaseViewStateReducer<CalendarPickerViewState>() {

    override val stateKey = key<CalendarPickerViewState>()

    override fun reduce(
        state: AppState,
        subState: CalendarPickerViewState,
        action: Action
    ) =
        when (action) {
            is DataLoadedAction.CalendarsChanged ->
                CalendarPickerViewState.CalendarsLoaded(
                    petAvatar = state.dataState.player!!.pet.avatar,
                    calendars = action.calendars.filter { it.isVisible }
                )
            else ->
                subState
        }

    override fun defaultState() = CalendarPickerViewState.Loading
}

sealed class CalendarPickerViewState : ViewState {

    object Loading : CalendarPickerViewState()
    data class CalendarsLoaded(
        val petAvatar: PetAvatar,
        val calendars: List<Calendar>
    ) : CalendarPickerViewState()
}

