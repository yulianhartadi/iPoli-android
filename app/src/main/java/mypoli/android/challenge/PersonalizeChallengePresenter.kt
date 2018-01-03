package mypoli.android.challenge

import mypoli.android.challenge.PersonalizeChallengeViewState.StateType.*
import mypoli.android.challenge.data.Challenge
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
    coroutineContext: CoroutineContext) : BaseMviPresenter<ViewStateRenderer<PersonalizeChallengeViewState>, PersonalizeChallengeViewState, PersonalizeChallengeIntent>(
    PersonalizeChallengeViewState(LOADING), coroutineContext) {
    override fun reduceState(intent: PersonalizeChallengeIntent, state: PersonalizeChallengeViewState): PersonalizeChallengeViewState {
        return when (intent) {

            is PersonalizeChallengeIntent.LoadData -> {

                val vms = intent.challenge.quests.map {
                    when (it) {
                        is Challenge.Quest.OneTime -> {
                            PersonalizeChallengeViewController.ChallengeQuestViewModel(it.text, it.selected, it)
                        }

                        is Challenge.Quest.Repeating -> {
                            PersonalizeChallengeViewController.ChallengeQuestViewModel(it.text, it.selected, it)
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
                    type = TOGGLE_QUEST,
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
                scheduleChallengeUseCase.execute(ScheduleChallengeUseCase.Params(intent.challenge))
                state.copy(
                    type = CHALLENGE_ACCEPTED
                )
            }
        }
    }

}