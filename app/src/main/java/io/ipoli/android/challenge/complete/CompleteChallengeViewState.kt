package io.ipoli.android.challenge.complete

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action

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