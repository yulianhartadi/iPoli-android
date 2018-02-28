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
import mypoli.android.reminder.view.picker.ReminderViewModel
import mypoli.android.repeatingquest.edit.EditRepeatingQuestViewState.StateType.*
import mypoli.android.repeatingquest.entity.FrequencyType
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.entity.frequencyType
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/26/18.
 */

sealed class EditRepeatingQuestAction : Action {
    data class Load(val repeatingQuestId : String) : EditRepeatingQuestAction()
    data class ChangeRepeatingPattern(val repeatingPattern: RepeatingPattern) :
        EditRepeatingQuestAction()

    data class ChangeStartTime(val time: Time?) : EditRepeatingQuestAction()
    data class ChangeDuration(val minutes: Int) : EditRepeatingQuestAction()
    data class ChangeReminder(val reminder: ReminderViewModel?) : EditRepeatingQuestAction()
    data class ChangeName(val name: String) : EditRepeatingQuestAction()
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
                    type = DATA_LOADED,
                    name = "Run 3 km",
                    startTime = Time.at(15, 15),
                    repeatingPattern = RepeatingPattern.Daily(),
                    frequencyType = FrequencyType.DAILY,
                    duration = 20,
                    reminder = Reminder("AAA", Time.Companion.at(15, 0), LocalDate.now()),
                    icon = null,
                    color = Color.PINK
                )
            }

            is EditRepeatingQuestAction.ChangeRepeatingPattern -> {
                subState.copy(
                    type = REPEATING_PATTERN_CHANGED,
                    repeatingPattern = action.repeatingPattern,
                    frequencyType = action.repeatingPattern.frequencyType
                )
            }

            is EditRepeatingQuestAction.ChangeStartTime -> {
                subState.copy(
                    type = START_TIME_CHANGED,
                    startTime = action.time
                )
            }

            is EditRepeatingQuestAction.ChangeDuration -> {
                subState.copy(
                    type = DURATION_CHANGED,
                    duration = action.minutes
                )
            }

            is EditRepeatingQuestAction.ChangeReminder -> {
                subState.copy(
                    type = REMINDER_CHANGED
//                    reminder = action.reminder
                )
            }

            is EditRepeatingQuestAction.ChangeName -> {
                subState.copy(
                    type = NAME_CHANGED,
                    name = action.name
                )
            }

            else -> subState
        }

    override fun defaultState() =
        EditRepeatingQuestViewState(
            type = LOADING,
            name = "",
            repeatingPattern = RepeatingPattern.Daily(),
            frequencyType = FrequencyType.DAILY,
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
    val frequencyType: FrequencyType,
    val duration: Int,
    val startTime: Time?,
    val reminder: Reminder?,
    val icon: Icon?,
    val color: Color
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        CHANGED,
        REPEATING_PATTERN_CHANGED,
        START_TIME_CHANGED,
        DURATION_CHANGED,
        REMINDER_CHANGED,
        NAME_CHANGED
    }
}

val EditRepeatingQuestViewState.reminderViewModel: ReminderViewModel?
    get() = reminder?.let { ReminderViewModel(it.message, 10) }