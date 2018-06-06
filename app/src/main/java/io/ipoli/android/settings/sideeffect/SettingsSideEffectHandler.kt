package io.ipoli.android.settings.sideeffect

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.notification.QuickDoNotificationUtil
import io.ipoli.android.common.redux.Action
import io.ipoli.android.myPoliApp
import io.ipoli.android.settings.SettingsAction
import io.ipoli.android.settings.usecase.*
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/17/18.
 */
object SettingsSideEffectHandler : AppSideEffectHandler() {

    private val savePlanDayTimeUseCase by required { savePlanDayTimeUseCase }
    private val savePlanDaysUseCase by required { savePlanDaysUseCase }
    private val saveTimeFormatUseCase by required { saveTimeFormatUseCase }
    private val saveTemperatureUnitUseCase by required { saveTemperatureUnitUseCase }
    private val saveQuickDoNotificationSettingUseCase by required { saveQuickDoNotificationSettingUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is SettingsAction.PlanDayTimeChanged ->
                savePlanDayTimeUseCase.execute(SavePlanDayTimeUseCase.Params(action.time))

            is SettingsAction.PlanDaysChanged ->
                savePlanDaysUseCase.execute(SavePlanDaysUseCase.Params(action.days))

            is SettingsAction.TimeFormatChanged ->
                saveTimeFormatUseCase.execute(SaveTimeFormatUseCase.Params(action.format))

            is SettingsAction.TemperatureUnitChanged ->
                saveTemperatureUnitUseCase.execute(SaveTemperatureUnitUseCase.Params(action.unit))

            is SettingsAction.ToggleQuickDoNotification -> {
                val p = saveQuickDoNotificationSettingUseCase.execute(
                    SaveQuickDoNotificationSettingUseCase.Params(
                        action.isEnabled
                    )
                )

                if (p.preferences.isQuickDoNotificationEnabled) {
                    QuickDoNotificationUtil.update(
                        myPoliApp.instance,
                        state.dataState.todayQuests,
                        p
                    )
                } else {
                    QuickDoNotificationUtil.disable(myPoliApp.instance)
                }

            }
        }
    }

    override fun canHandle(action: Action) = action is SettingsAction

}