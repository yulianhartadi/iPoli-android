package io.ipoli.android.settings.sideeffect

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.settings.SettingsAction
import io.ipoli.android.settings.usecase.SavePlanDayTimeUseCase
import io.ipoli.android.settings.usecase.SavePlanDaysUseCase
import io.ipoli.android.settings.usecase.SaveTemperatureUnitUseCase
import io.ipoli.android.settings.usecase.SaveTimeFormatUseCase
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
        }
    }

    override fun canHandle(action: Action) = action is SettingsAction

}