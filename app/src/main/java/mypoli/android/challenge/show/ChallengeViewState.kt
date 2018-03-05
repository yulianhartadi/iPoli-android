package mypoli.android.challenge.show

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/05/2018.
 */

sealed class ChallengeAction : Action {
    data class Load(val challengeId: String) : ChallengeAction()
}

object ChallengeReducer : BaseViewStateReducer<ChallengeViewState>() {
    override fun reduce(
        state: AppState,
        subState: ChallengeViewState,
        action: Action
    ): ChallengeViewState {
        return when (action) {
            is ChallengeAction.Load -> {
                subState
            }
            else -> subState
        }
    }

    override fun defaultState() = ChallengeViewState("")

    override val stateKey = key<ChallengeViewState>()

}

data class ChallengeViewState(val name: String) : ViewState