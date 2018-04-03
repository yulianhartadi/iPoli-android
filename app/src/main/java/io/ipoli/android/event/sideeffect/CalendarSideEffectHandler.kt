package io.ipoli.android.event.sideeffect

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.event.calendar.picker.CalendarPickerAction
import io.ipoli.android.event.usecase.SaveSyncCalendarsUseCase
import io.ipoli.android.settings.SettingsAction
import io.ipoli.android.store.powerup.PowerUpStoreAction
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/11/2018.
 */
class CalendarSideEffectHandler : AppSideEffectHandler() {

    private val calendarRepository by required { calendarRepository }
    private val saveSyncCalendarsUseCase by required { saveSyncCalendarsUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is CalendarPickerAction.Load ->
                dispatch(DataLoadedAction.CalendarsChanged(calendarRepository.findAll()))

            is SettingsAction.SyncCalendarsSelected ->
                saveSyncCalendarsUseCase.execute(
                    SaveSyncCalendarsUseCase.Params(action.calendarIds)
                )

            is PowerUpStoreAction.SyncCalendarsSelected ->
                saveSyncCalendarsUseCase.execute(
                    SaveSyncCalendarsUseCase.Params(action.calendarIds)
                )

            SettingsAction.DisableCalendarsSync ->
                saveSyncCalendarsUseCase.execute(SaveSyncCalendarsUseCase.Params(setOf()))

        }
    }

    override fun canHandle(action: Action) =
        action == CalendarPickerAction.Load
            || action == SettingsAction.DisableCalendarsSync
            || action is SettingsAction.SyncCalendarsSelected
            || action is PowerUpStoreAction.SyncCalendarsSelected


}