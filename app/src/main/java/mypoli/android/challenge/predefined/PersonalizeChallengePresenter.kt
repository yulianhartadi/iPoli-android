package mypoli.android.challenge.predefined

import mypoli.android.challenge.predefined.PersonalizeChallengeViewState.StateType.*
import mypoli.android.challenge.predefined.entity.PredefinedChallengeData
import mypoli.android.challenge.usecase.SaveChallengeUseCase
import mypoli.android.challenge.predefined.usecase.SchedulePredefinedChallengeUseCase
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import org.threeten.bp.LocalDate
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class PersonalizeChallengePresenter(
    private val schedulePredefinedChallengeUseCase: SchedulePredefinedChallengeUseCase,
    private val saveChallengeUseCase: SaveChallengeUseCase,
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
                    val baseQuests = schedulePredefinedChallengeUseCase.execute(
                        SchedulePredefinedChallengeUseCase.Params(challenge)
                    )
                    saveChallengeUseCase.execute(
                        SaveChallengeUseCase.Params.WithNewQuests(
                            id = "",
                            name = predefinedChallenge.title,
                            color = predefinedChallenge.color,
                            icon = predefinedChallenge.icon,
                            difficulty = predefinedChallenge.difficulty,
                            motivations = predefinedChallenge.motivations,
                            end = LocalDate.now().plusDays((predefinedChallenge.durationDays - 1).toLong()),
                            quests = baseQuests
                        )
                    )

                    state.copy(
                        type = CHALLENGE_ACCEPTED
                    )
                }
            }
        }
    }

}