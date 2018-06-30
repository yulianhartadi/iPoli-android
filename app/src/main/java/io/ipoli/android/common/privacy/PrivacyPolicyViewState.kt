package io.ipoli.android.common.privacy

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer

import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState

sealed class PrivacyPolicyAction : Action

object PrivacyPolicyReducer : BaseViewStateReducer<PrivacyPolicyViewState>() {
    override val stateKey = key<PrivacyPolicyViewState>()

    override fun reduce(
        state: AppState,
        subState: PrivacyPolicyViewState,
        action: Action
    ): PrivacyPolicyViewState {
        return subState
    }

    override fun defaultState() = PrivacyPolicyViewState(
        type = PrivacyPolicyViewState.StateType.LOADING
    )

}

data class PrivacyPolicyViewState(
    val type: StateType
) : BaseViewState() {

    enum class StateType {
        LOADING
    }
}