package mypoli.android.challenge

import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class PersonalizeChallengePresenter(coroutineContext: CoroutineContext) : BaseMviPresenter<ViewStateRenderer<PersonalizeChallengeViewState>, PersonalizeChallengeViewState, PersonalizeChallengeIntent>(
    PersonalizeChallengeViewState(PersonalizeChallengeViewState.StateType.DATA_LOADED), coroutineContext) {
    override fun reduceState(intent: PersonalizeChallengeIntent, state: PersonalizeChallengeViewState): PersonalizeChallengeViewState {
        return state
    }

}