package io.ipoli.android.dailychallenge.sideeffect

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.dailychallenge.DailyChallengeAction
import io.ipoli.android.dailychallenge.DailyChallengeViewState
import io.ipoli.android.dailychallenge.usecase.LoadDailyChallengeUseCase
import io.ipoli.android.dailychallenge.usecase.SaveDailyChallengeQuestIdsUseCase
import io.ipoli.android.planday.PlanDayAction
import io.ipoli.android.planday.PlanDayViewState
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/28/18.
 */
object DailyChallengeSideEffectHandler : AppSideEffectHandler() {

    private val loadDailyChallengeUseCase by required { loadDailyChallengeUseCase }
    private val saveDailyChallengeQuestIdsUseCase by required { saveDailyChallengeQuestIdsUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            DailyChallengeAction.Load -> {
                dispatch(
                    DailyChallengeAction.Loaded(
                        loadDailyChallengeUseCase.execute(
                            LoadDailyChallengeUseCase.Params()
                        )
                    )
                )
            }

            DailyChallengeAction.Save -> {
                val s = state.stateFor(DailyChallengeViewState::class.java)
                saveDailyChallengeQuestIdsUseCase.execute(SaveDailyChallengeQuestIdsUseCase.Params(
                    questIds = if (s.selectedQuests == null) {
                        emptyList()
                    } else {
                        s.selectedQuests.map { it.id }
                    }
                ))
            }

            PlanDayAction.LoadToday -> {
                dispatch(
                    PlanDayAction.DailyChallengeLoaded(
                        loadDailyChallengeUseCase.execute(
                            LoadDailyChallengeUseCase.Params()
                        )
                    )
                )
            }

            PlanDayAction.Done -> {
                val s = state.stateFor(PlanDayViewState::class.java)
                saveDailyChallengeQuestIdsUseCase.execute(
                    SaveDailyChallengeQuestIdsUseCase.Params(
                        questIds = s.dailyChallengeQuestIds ?: emptyList()
                    )
                )
            }
        }
    }

    override fun canHandle(action: Action) =
        action is DailyChallengeAction
            || action is PlanDayAction

}