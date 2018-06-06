package io.ipoli.android.player.sideeffect

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.player.ProfileAction
import io.ipoli.android.player.usecase.FindAverageFocusedDurationForPeriodUseCase
import io.ipoli.android.player.usecase.SaveProfileUseCase
import space.traversal.kapsule.required

object ProfileSideEffectHandler : AppSideEffectHandler() {

    private val findDailyChallengeStreakUseCase by required { findDailyChallengeStreakUseCase }
    private val findAverageProductiveDurationForPeriodUseCase by required { findAverageFocusedDurationForPeriodUseCase }
    private val saveProfileUseCase by required { saveProfileUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is ProfileAction.Load -> {
                dispatch(
                    DataLoadedAction.ProfileStatsChanged(
                        findDailyChallengeStreakUseCase.execute(Unit),
                        findAverageProductiveDurationForPeriodUseCase.execute(
                            FindAverageFocusedDurationForPeriodUseCase.Params(dayPeriod = 7)
                        )
                    )
                )
            }

            is ProfileAction.Save -> {
                saveProfileUseCase.execute(
                    SaveProfileUseCase.Params(
                        displayName = action.displayName,
                        bio = action.bio
                    )
                )
            }
        }
    }

    override fun canHandle(action: Action) = action is ProfileAction

}