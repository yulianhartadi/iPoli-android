package io.ipoli.android.event.calendar.picker

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.event.Calendar
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.player.data.Player

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/11/2018.
 */

sealed class CalendarPickerAction : Action {
    object Load : CalendarPickerAction()
    object SyncSelectedCalendars : CalendarPickerAction()

    data class ToggleSelectedCalendar(
        val isSelected: Boolean,
        val id: String,
        val name: String
    ) : CalendarPickerAction() {
        override fun toMap() = mapOf("isSelected" to isSelected, "id" to id, "name" to name)
    }
}

object CalendarPickerReducer :
    BaseViewStateReducer<CalendarPickerViewState>() {

    override val stateKey = key<CalendarPickerViewState>()

    override fun reduce(
        state: AppState,
        subState: CalendarPickerViewState,
        action: Action
    ) =
        when (action) {
            is DataLoadedAction.CalendarsChanged -> {
                val newState = subState.copy(
                    calendars = action.calendars.filter { it.isVisible }
                )
                state.dataState.player?.let {
                    newState.copy(
                        type = CalendarPickerViewState.StateType.CALENDAR_DATA_CHANGED,
                        petAvatar = it.pet.avatar,
                        syncCalendars = it.preferences.syncCalendars
                    )
                } ?: newState.copy(type = CalendarPickerViewState.StateType.LOADING)
            }

            is CalendarPickerAction.ToggleSelectedCalendar -> {
                val syncCalendar = Player.Preferences.SyncCalendar(action.id, action.name)
                val newSyncCalendars = if (subState.syncCalendars.contains(syncCalendar)) {
                    subState.syncCalendars - syncCalendar
                } else {
                    subState.syncCalendars + syncCalendar
                }

                subState.copy(
                    type = CalendarPickerViewState.StateType.CALENDAR_DATA_CHANGED,
                    syncCalendars = newSyncCalendars
                )
            }

            is CalendarPickerAction.SyncSelectedCalendars -> {
                subState.copy(
                    type = CalendarPickerViewState.StateType.CALENDARS_SELECTED
                )
            }

            else ->
                subState
        }

    override fun defaultState() =
        CalendarPickerViewState(
            type = CalendarPickerViewState.StateType.LOADING,
            petAvatar = PetAvatar.BEAR,
            calendars = emptyList(),
            syncCalendars = emptySet()
        )
}

data class CalendarPickerViewState(
    val type: StateType,
    val petAvatar: PetAvatar,
    val calendars: List<Calendar>,
    val syncCalendars: Set<Player.Preferences.SyncCalendar>
) : BaseViewState() {
    enum class StateType { LOADING, CALENDAR_DATA_CHANGED, CALENDARS_SELECTED}
}

