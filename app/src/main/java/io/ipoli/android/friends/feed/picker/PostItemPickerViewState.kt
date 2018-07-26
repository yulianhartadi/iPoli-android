package io.ipoli.android.friends.feed.picker

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.friends.feed.picker.ItemToSharePickerViewState.StateType.DATA_CHANGED
import io.ipoli.android.friends.feed.picker.ItemToSharePickerViewState.StateType.LOADING
import io.ipoli.android.quest.Quest

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 7/16/18.
 */
sealed class PostItemPickerAction : Action {
    data class SelectQuest(val questId: String) : PostItemPickerAction()
    data class DeselectQuest(val questId: String) : PostItemPickerAction()
    data class SelectChallenge(val challengeId: String) : PostItemPickerAction()
    data class DeselectChallenge(val challengeId: String) : PostItemPickerAction()

    object Load : PostItemPickerAction()
    object Share : PostItemPickerAction()
}

object PostItemPickerReducer : BaseViewStateReducer<ItemToSharePickerViewState>() {

    override val stateKey = key<ItemToSharePickerViewState>()

    override fun reduce(
        state: AppState,
        subState: ItemToSharePickerViewState,
        action: Action
    ): ItemToSharePickerViewState {
        return when (action) {
            is DataLoadedAction.PostItemPickerItemsChanged -> {
                val qs = action.quests ?: subState.quests
                val chs = action.challenges ?: subState.challenges
                if (qs == null || chs == null) {
                        subState.copy(
                            type = LOADING
                        )
                    } else {
                        subState.copy(
                            type = DATA_CHANGED,
                            quests = qs,
                            challenges = chs
                        )
                    }
            }

            is PostItemPickerAction.SelectQuest ->
                subState.copy(
                    type = DATA_CHANGED,
                    selectedQuestIds = subState.selectedQuestIds + action.questId
                )

            is PostItemPickerAction.DeselectQuest ->
                subState.copy(
                    type = DATA_CHANGED,
                    selectedQuestIds = subState.selectedQuestIds - action.questId
                )
            
            is PostItemPickerAction.SelectChallenge ->
                subState.copy(
                    type = DATA_CHANGED,
                    selectedChallengeIds = subState.selectedChallengeIds + action.challengeId
                )

            is PostItemPickerAction.DeselectChallenge ->
                subState.copy(
                    type = DATA_CHANGED,
                    selectedChallengeIds = subState.selectedChallengeIds - action.challengeId
                )


            else -> subState
        }
    }

    override fun defaultState() = ItemToSharePickerViewState(
        type = LOADING,
        quests = null,
        challenges = null,
        selectedQuestIds = emptyList(),
        selectedChallengeIds = emptyList()
    )

}


data class ItemToSharePickerViewState(
    val type: StateType,
    val quests: List<Quest>?,
    val challenges: List<Challenge>?,
    val selectedQuestIds: List<String>,
    val selectedChallengeIds: List<String>
) : BaseViewState() {

    enum class StateType {
        LOADING,
        DATA_CHANGED
    }
}