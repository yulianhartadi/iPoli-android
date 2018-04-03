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
            val selectedCalendars = state.dataState.player?.syncCalendarIds?.size ?: 0
            createChangedState(selectedCalendars)
        }

        is DataLoadedAction.PlayerChanged -> {
            val selectedCalendars = action.player.syncCalendarIds.size
            createChangedState(selectedCalendars)
        }

        else -> subState
    }

    private fun createChangedState(selectedCalendars: Int): SettingsViewState {
        return SettingsViewState.Changed(
            selectedCalendars = selectedCalendars,
            isCalendarSyncEnabled = selectedCalendars > 0
        )
    }

    override fun defaultState() = SettingsViewState.Loading

    override val stateKey = key<SettingsViewState>()

}

sealed class SettingsViewState : ViewState {
    object Loading : SettingsViewState()
    data class Changed(
        val isCalendarSyncEnabled: Boolean,
        val selectedCalendars: Int
    ) : SettingsViewState()
}