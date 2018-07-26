package io.ipoli.android.challenge.list

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/05/2018.
 */

sealed class ChallengeListAction : Action {
    object Load : ChallengeListAction()
    object AddChallenge : ChallengeListAction()
}

object ChallengeListReducer : BaseViewStateReducer<ChallengeListViewState>() {

    override fun reduce(
        state: AppState,
        subState: ChallengeListViewState,
        action: Action
    ) =
        when (action) {
            is ChallengeListAction.Load ->
                createState(state.dataState.challenges, subState)

            is ChallengeListAction.AddChallenge ->
                subState.copy(
                    type = ChallengeListViewState.StateType.SHOW_ADD
                )

            is DataLoadedAction.ChallengesChanged ->
                createState(action.challenges, subState)

            else -> subState
        }

    private fun createState(challenges: List<Challenge>?, state: ChallengeListViewState) =
        when {
            challenges == null -> state.copy(type = ChallengeListViewState.StateType.LOADING)
            challenges.isEmpty() -> state.copy(type = ChallengeListViewState.StateType.EMPTY)
            else -> state.copy(
                type = ChallengeListViewState.StateType.DATA_CHANGED,
                challenges = createChallengeItems(challenges)
            )
        }



    override fun defaultState() = ChallengeListViewState(
        type = ChallengeListViewState.StateType.LOADING,
        challenges = emptyList()
    )

    override val stateKey = key<ChallengeListViewState>()
}

data class ChallengeListViewState(val type: StateType, val challenges: List<ChallengeItem>) :
    BaseViewState() {

    enum class StateType {
        LOADING, EMPTY, SHOW_ADD, DATA_CHANGED
    }
}