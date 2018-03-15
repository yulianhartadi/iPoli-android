package mypoli.android.challenge.complete

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/15/2018.
 */

sealed class CompleteChallengeAction : Action {

}

object CompleteChallengeReducer : BaseViewStateReducer<CompleteChallengeViewState>() {

    override fun reduce(
        state: AppState,
        subState: CompleteChallengeViewState,
        action: Action
    ): CompleteChallengeViewState {
        return subState
    }

    override fun defaultState() = CompleteChallengeViewState.Loading

    override val stateKey get() = key<CompleteChallengeViewState>()

}

sealed class CompleteChallengeViewState : ViewState {

    object Loading : CompleteChallengeViewState()
}