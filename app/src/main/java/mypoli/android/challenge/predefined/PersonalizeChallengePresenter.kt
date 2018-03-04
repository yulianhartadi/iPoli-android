package mypoli.android.challenge.predefined

import mypoli.android.challenge.predefined.PersonalizeChallengeViewState.StateType.*
import mypoli.android.challenge.predefined.entity.PredefinedChallengeData
import mypoli.android.challenge.usecase.ScheduleChallengeUseCase
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class PersonalizeChallengePresenter(
    private val scheduleChallengeUseCase: ScheduleChallengeUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<PersonalizeChallengeViewState>, PersonalizeChallengeViewState, PersonalizeChallengeIntent>(
    PersonalizeChallengeViewState(LOADING), coroutineContext
) {
    override fun reduceState(
        intent: PersonalizeChallengeIntent,
        state: PersonalizeChallengeViewState
    ): PersonalizeChallengeViewState {
        return when (intent) {

            is PersonalizeChallengeIntent.LoadData -> {

                val vms = intent.challenge.quests.map {
                    when (it) {
                        is PredefinedChallengeData.Quest.OneTime -> {
                            PersonalizeChallengeViewController.ChallengeQuestViewModel(
                                it.text,
                                it.selected,
                                it
                            )
                        }

                        is PredefinedChallengeData.Quest.Repeating -> {
                            PersonalizeChallengeViewController.ChallengeQuestViewModel(
                                it.text,
                                it.selected,
                                it
                            )
                        }
                    }
                }

                state.copy(
                    type = DATA_LOADED,
                    viewModels = vms
                )
            }

            is PersonalizeChallengeIntent.ToggleSelected -> {
                state.copy(
                    type = TOGGLE_SELECTED_QUEST,
                    viewModels = state.viewModels.map {
                        if (it == intent.quest) {
                            it.copy(isSelected = !it.isSelected)
                        } else {
                            it
                        }
                    }
                )
            }

            is PersonalizeChallengeIntent.AcceptChallenge -> {
                val predefinedChallenge = intent.challenge

                val quests = state.viewModels.filter { it.isSelected }.map { it.quest }

                if (quests.isEmpty()) {
                    state.copy(
                        type = NO_QUESTS_SELECTED
                    )
                } else {
                    val challenge =
                        PredefinedChallengeData(
                            predefinedChallenge.category,
                            quests,
                            predefinedChallenge.durationDays
                        )
                    scheduleChallengeUseCase.execute(ScheduleChallengeUseCase.Params(challenge))
                    state.copy(
                        type = CHALLENGE_ACCEPTED
                    )
                }
            }
        }
    }

}