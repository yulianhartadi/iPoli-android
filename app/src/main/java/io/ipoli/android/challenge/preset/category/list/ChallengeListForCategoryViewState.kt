package io.ipoli.android.challenge.preset.category.list

import io.ipoli.android.challenge.preset.PresetChallenge
import io.ipoli.android.challenge.preset.category.list.ChallengeListForCategoryViewState.StateType.*
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */

sealed class ChallengeListForCategoryAction : Action {
    object ErrorLoadingChallenges : ChallengeListForCategoryAction()

    data class Load(val category : PresetChallenge.Category) : ChallengeListForCategoryAction()
}

object ChallengeListForCategoryReducer : BaseViewStateReducer<ChallengeListForCategoryViewState>() {

    override val stateKey = key<ChallengeListForCategoryViewState>()

    override fun reduce(
        state: AppState,
        subState: ChallengeListForCategoryViewState,
        action: Action
    ): ChallengeListForCategoryViewState {

        val player = state.dataState.player

        val challengeListState = subState.copy(
            playerGems = player?.gems ?: 0
        )

        return when (action) {
            is DataLoadedAction.PresetChallengeListForCategoryChanged -> {
                challengeListState.copy(
                    type = DATA_CHANGED,
                    challenges = action.challenges,
                    category = action.category
                )
            }

            is ChallengeListForCategoryAction.ErrorLoadingChallenges ->
                challengeListState.copy(
                    type = NO_INTERNET
                )

            else -> challengeListState
        }
    }

    override fun defaultState(): ChallengeListForCategoryViewState {
        return ChallengeListForCategoryViewState(
            type = LOADING,
            challenges = null
        )
    }
}

data class ChallengeListForCategoryViewState(
    val type: StateType,
    val category: PresetChallenge.Category? = null,
    val playerGems: Int = 0,
    val challenges: List<PresetChallenge>?
) : BaseViewState() {

    enum class StateType {
        LOADING,
        DATA_CHANGED,
        NO_INTERNET
    }
}