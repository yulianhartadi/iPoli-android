package io.ipoli.android.growth.sideeffect

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.growth.GrowthAction
import io.ipoli.android.growth.usecase.CalculateGrowthStatsUseCase
import space.traversal.kapsule.required

object GrowthSideEffectHandler : AppSideEffectHandler() {

    private val calculateGrowthStatsUseCase by required { calculateGrowthStatsUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is GrowthAction.Load -> {
                val result = calculateGrowthStatsUseCase.execute(CalculateGrowthStatsUseCase.Params())
                dispatch(
                    DataLoadedAction.GrowthChanged(
                        result.todayGrowth,
                        result.weeklyGrowth,
                        result.monthlyGrowth
                    )
                )
            }
        }
    }

    override fun canHandle(action: Action) = action is GrowthAction.Load

}