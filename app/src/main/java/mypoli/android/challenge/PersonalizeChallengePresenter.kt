package mypoli.android.challenge

import mypoli.android.challenge.PersonalizeChallengeViewState.StateType.CHALLENGE_ACCEPTED
import mypoli.android.challenge.PersonalizeChallengeViewState.StateType.DATA_LOADED
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
    PersonalizeChallengeViewState(DATA_LOADED), coroutineContext) {
    override fun reduceState(intent: PersonalizeChallengeIntent, state: PersonalizeChallengeViewState): PersonalizeChallengeViewState {
        return when (intent) {
            is PersonalizeChallengeIntent.AcceptChallenge -> {
                scheduleChallengeUseCase.execute(ScheduleChallengeUseCase.Params(intent.challenge))
                state.copy(
                    type = CHALLENGE_ACCEPTED
                )
            }
        }
    }

}