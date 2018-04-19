package io.ipoli.android.settings

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 4/2/18.
 */
sealed class SettingsAction : Action {
    data class SyncCalendarsSelected(val calendarIds: Set<String>) : SettingsAction()
    object Load : SettingsAction()
    object DisableCalendarsSync : SettingsAction()
}

object SettingsReducer : BaseViewStateReducer<SettingsViewState>() {
    override fun reduce(
        state: AppState,
        subState: SettingsViewState,
        action: Action
    ) = when (action) {

        SettingsAction.Load -> {
            val selectedCalendars = state.dataState.player!!.preferences.syncCalendarIds.size
            createChangedState(
                playerId = state.dataState.player.id,
                selectedCalendars = selectedCalendars
            )
        }

        is DataLoadedAction.PlayerChanged -> {
            val selectedCalendars = action.player.preferences.syncCalendarIds.size
            createChangedState(playerId = action.player.id, selectedCalendars = selectedCalendars)
        }

        else -> subState
    }

    private fun createChangedState(playerId: String, selectedCalendars: Int) =
        SettingsViewState.Changed(
            playerId = playerId,
            selectedCalendars = selectedCalendars,
            isCalendarSyncEnabled = selectedCalendars > 0
        )

    override fun defaultState() = SettingsViewState.Loading

    override val stateKey = key<SettingsViewState>()

}

sealed class SettingsViewState : ViewState {
    object Loading : SettingsViewState()
    data class Changed(
        val playerId: String,
        val isCalendarSyncEnabled: Boolean,
        val selectedCalendars: Int
    ) : SettingsViewState()
}