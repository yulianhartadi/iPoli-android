package io.ipoli.android.challenge.list

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.challenge.list.ChallengeListViewState.ChallengeItem.*
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action

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
                createState(state.dataState.challenges)

            is ChallengeListAction.AddChallenge ->
                ChallengeListViewState.ShowAdd

            is DataLoadedAction.ChallengesChanged ->
                createState(action.challenges)

            else -> subState
        }

    private fun createState(challenges: List<Challenge>) =
        when {
            challenges.isEmpty() -> ChallengeListViewState.Empty
            else -> ChallengeListViewState.Changed(createChallengeItems(challenges))
        }

    private fun createChallengeItems(challenges: List<Challenge>): List<ChallengeListViewState.ChallengeItem> {
        val (incomplete, complete) = challenges.partition { it.completedAtDate == null }

        val incompleteItems = incomplete.map { Incomplete(it) }
        return when {
            complete.isEmpty() -> incompleteItems
            else -> incompleteItems +
                CompleteLabel +
                complete.sortedByDescending { it.completedAtDate!! }.map { Complete(it) }
        }
    }

    override fun defaultState() = ChallengeListViewState.Loading

    override val stateKey = key<ChallengeListViewState>()
}

sealed class ChallengeListViewState : ViewState {

    object Loading : ChallengeListViewState()
    object Empty : ChallengeListViewState()
    object ShowAdd : ChallengeListViewState()

    data class Changed(val challenges: List<ChallengeItem>) : ChallengeListViewState()

    sealed class ChallengeItem {

        data class Incomplete(val challenge: Challenge) : ChallengeItem()

        object CompleteLabel : ChallengeItem()
        data class Complete(val challenge: Challenge) : ChallengeItem()
    }

}