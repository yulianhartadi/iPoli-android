package io.ipoli.android.friends.feed.picker

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.friends.feed.picker.PostItemPickerViewState.StateType.DATA_CHANGED
import io.ipoli.android.friends.feed.picker.PostItemPickerViewState.StateType.LOADING
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.quest.Quest

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 7/16/18.
 */
sealed class PostItemPickerAction : Action {
    object Load : PostItemPickerAction()
}

object PostItemPickerReducer : BaseViewStateReducer<PostItemPickerViewState>() {

    override val stateKey = key<PostItemPickerViewState>()

    override fun reduce(
        state: AppState,
        subState: PostItemPickerViewState,
        action: Action
    ): PostItemPickerViewState {
        return when (action) {
            is DataLoadedAction.PostItemPickerItemsChanged -> {
                val qs = action.quests ?: subState.quests
                val hs = action.habits ?: subState.habits
                val chs = action.challenges ?: subState.challenges
                if (qs == null || chs == null || hs == null) {
                        subState.copy(
                            type = LOADING
                        )
                    } else {
                        subState.copy(
                            type = DATA_CHANGED,
                            quests = qs,
                            habits = hs,
                            challenges = chs
                        )
                    }
            }

            else -> subState
        }
    }

    override fun defaultState() = PostItemPickerViewState(
        type = LOADING,
        quests = null,
        habits = null,
        challenges = null
    )

}


data class PostItemPickerViewState(
    val type: StateType,
    val quests: List<Quest>?,
    val habits: List<Habit>?,
    val challenges: List<Challenge>?
) : BaseViewState() {

    enum class StateType {
        LOADING,
        DATA_CHANGED
    }
}