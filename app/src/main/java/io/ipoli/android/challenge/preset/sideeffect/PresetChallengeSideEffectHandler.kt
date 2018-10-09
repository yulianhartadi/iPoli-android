package io.ipoli.android.challenge.preset.sideeffect

import io.ipoli.android.challenge.preset.PresetChallengeAction
import io.ipoli.android.challenge.preset.category.list.ChallengeListForCategoryAction
import io.ipoli.android.challenge.preset.usecase.UnlockPresetChallengeUseCase
import io.ipoli.android.challenge.usecase.CreateChallengeFromPresetUseCase
import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import org.threeten.bp.LocalDate
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 9/28/18.
 */
object PresetChallengeSideEffectHandler : AppSideEffectHandler() {

    private val presetChallengeRepository by required { presetChallengeRepository }
    private val createChallengeFromPresetUseCase by required { createChallengeFromPresetUseCase }
    private val unlockPresetChallengeUseCase by required { unlockPresetChallengeUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is ChallengeListForCategoryAction.Load -> {
                try {
                    dispatch(
                        DataLoadedAction.PresetChallengeListForCategoryChanged(
                            category = action.category,
                            challenges = presetChallengeRepository.findForCategory(action.category)
                        )
                    )
                } catch (e: Throwable) {
                    dispatch(ChallengeListForCategoryAction.ErrorLoadingChallenges)
                }
            }

            is PresetChallengeAction.Accept -> {
                createChallengeFromPresetUseCase.execute(
                    CreateChallengeFromPresetUseCase.Params(
                        preset = action.challenge,
                        schedule = action.schedule,
                        tags = action.tags,
                        startDate = LocalDate.now(),
                        questsStartTime = action.startTime,
                        playerPhysicalCharacteristics = action.physicalCharacteristics
                    )
                )
            }

            is PresetChallengeAction.Unlock -> {
                val result = unlockPresetChallengeUseCase.execute(
                    UnlockPresetChallengeUseCase.Params(action.challenge)
                )
                when (result) {
                    is UnlockPresetChallengeUseCase.Result.Unlocked -> {
                        dispatch(PresetChallengeAction.Unlocked)
                    }

                    UnlockPresetChallengeUseCase.Result.TooExpensive -> {
                        dispatch(
                            PresetChallengeAction.ChallengeTooExpensive
                        )
                    }
                }
            }

        }
    }

    override fun canHandle(action: Action) =
        action is ChallengeListForCategoryAction
            || action is PresetChallengeAction

}