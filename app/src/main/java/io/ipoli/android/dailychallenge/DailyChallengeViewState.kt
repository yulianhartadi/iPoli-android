package io.ipoli.android.dailychallenge

import io.ipoli.android.Constants
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.mvi.BaseViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.dailychallenge.DailyChallengeViewState.StateType.*
import io.ipoli.android.dailychallenge.data.DailyChallenge
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.quest.Quest

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/24/18.
 */
sealed class DailyChallengeAction : Action {
    data class AddQuest(val questId: String) : DailyChallengeAction()
    data class RemoveQuest(val questId: String) : DailyChallengeAction()

    object Load : DailyChallengeAction()
    object Save : DailyChallengeAction()

    data class Loaded(val dailyChallenge: DailyChallenge) : DailyChallengeAction()
}

object DailyChallengeReducer : BaseViewStateReducer<DailyChallengeViewState> () {
    override val stateKey = key<DailyChallengeViewState>()

    override fun reduce(
        state: AppState,
        subState: DailyChallengeViewState,
        action: Action
    ): DailyChallengeViewState {
        return when (action) {
            is DailyChallengeAction.Loaded -> {
                val player = state.dataState.player
                val todayQuests = state.dataState.todayQuests
                val selectedQuests =
                    todayQuests.filter { action.dailyChallenge.questIds.contains(it.id) }
                subState.copy(
                    type = if (player != null) DATA_CHANGED else LOADING,
                    petAvatar = player?.pet?.avatar ?: subState.petAvatar,
                    selectedQuests = selectedQuests,
                    todayQuests = todayQuests - selectedQuests,
                    isCompleted = action.dailyChallenge.isCompleted
                )
            }

            is DataLoadedAction.TodayQuestsChanged -> {
                subState.copy(
                    type = DATA_CHANGED,
                    todayQuests = action.quests - subState.selectedQuests!!
                )
            }


            is DataLoadedAction.PlayerChanged -> {
                val player = action.player
                subState.copy(
                    type = DATA_CHANGED,
                    petAvatar = player.pet.avatar
                )
            }

            is DailyChallengeAction.AddQuest -> {
                if (subState.selectedQuests!!.size == Constants.DAILY_CHALLENGE_QUEST_COUNT) {
                    subState.copy(
                        type = MAX_QUESTS_REACHED
                    )
                } else {
                    val quest = subState.todayQuests!!.first { it.id == action.questId }
                    subState.copy(
                        type = DATA_CHANGED,
                        selectedQuests = subState.selectedQuests + quest,
                        todayQuests = subState.todayQuests - quest
                    )
                }

            }

            is DailyChallengeAction.RemoveQuest -> {
                val quest = subState.selectedQuests!!.first { it.id == action.questId }
                subState.copy(
                    type = DATA_CHANGED,
                    selectedQuests = subState.selectedQuests - quest,
                    todayQuests = subState.todayQuests!! + quest
                )
            }

            else -> subState
        }
    }

    override fun defaultState() = DailyChallengeViewState(
        type = LOADING,
        petAvatar = null,
        selectedQuests = null,
        todayQuests = null,
        isCompleted = false
    )
}

data class DailyChallengeViewState(
    val type: StateType,
    val petAvatar: PetAvatar?,
    val selectedQuests: List<Quest>?,
    val todayQuests: List<Quest>?,
    val isCompleted: Boolean
    ) : BaseViewState() {

    enum class StateType {
        LOADING,
        DATA_CHANGED,
        MAX_QUESTS_REACHED
    }
}