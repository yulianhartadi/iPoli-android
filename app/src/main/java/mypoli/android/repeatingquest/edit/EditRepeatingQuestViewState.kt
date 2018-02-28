package mypoli.android.repeatingquest.edit

import mypoli.android.Constants
import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.datetime.Time
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.quest.Color
import mypoli.android.quest.Icon
import mypoli.android.quest.Reminder
import mypoli.android.repeatingquest.entity.RepeatingPattern

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/26/18.
 */

sealed class EditRepeatingQuestAction : Action {
    data class Load(val repeatingQuestId : String) : EditRepeatingQuestAction()
}


object EditRepeatingQuestReducer : BaseViewStateReducer<EditRepeatingQuestViewState>() {

    override val stateKey = key<EditRepeatingQuestViewState>()

    override fun reduce(
        state: AppState,
        subState: EditRepeatingQuestViewState,
        action: Action
    ) =
        when (action) {
            is EditRepeatingQuestAction.Load -> {
                subState.copy(
                    type = EditRepeatingQuestViewState.StateType.CHANGED,
                    name = "Run 3 km",
                    startTime = Time.at(15, 15)
                )
            }

            else -> subState
        }

    override fun defaultState() =
        EditRepeatingQuestViewState(
            type = EditRepeatingQuestViewState.StateType.LOADING,
            name = "",
            repeatingPattern = RepeatingPattern.Daily(),
            duration = Constants.QUEST_MIN_DURATION,
            startTime = null,
            reminder = null,
            icon = null,
            color = Color.GREEN
        )

}


data class EditRepeatingQuestViewState(
    val type: EditRepeatingQuestViewState.StateType,
    val name: String,
    val repeatingPattern: RepeatingPattern,
    val duration: Int,
    val startTime: Time?,
    val reminder: Reminder?,
    val icon: Icon?,
    val color: Color
) : ViewState {
    enum class StateType {
        LOADING,
        CHANGED
    }
}