package io.ipoli.android.repeatingquest.edit

import io.ipoli.android.Constants
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.Reminder
import io.ipoli.android.quest.toMinutesFromStart
import io.ipoli.android.quest.reminder.picker.ReminderViewModel
import io.ipoli.android.repeatingquest.edit.EditRepeatingQuestViewState.StateType.*
import io.ipoli.android.repeatingquest.entity.RepeatType
import io.ipoli.android.repeatingquest.entity.RepeatingPattern
import io.ipoli.android.repeatingquest.entity.repeatType
import io.ipoli.android.repeatingquest.sideeffect.RepeatingQuestSideEffectHandler

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 2/26/18.
 */

sealed class EditRepeatingQuestAction : Action {
    data class Load(val repeatingQuestId: String) : EditRepeatingQuestAction()
    data class ChangeRepeatingPattern(val repeatingPattern: RepeatingPattern) :
        EditRepeatingQuestAction()

    data class ChangeStartTime(val time: Time?) : EditRepeatingQuestAction()
    data class ChangeDuration(val minutes: Int) : EditRepeatingQuestAction()
    data class ChangeReminder(val reminder: ReminderViewModel?) : EditRepeatingQuestAction()
    data class ChangeName(val name: String) : EditRepeatingQuestAction()
    object Save : EditRepeatingQuestAction()
    data class ChangeColor(val color: Color) : EditRepeatingQuestAction()
    data class ChangeIcon(val icon: Icon?) : EditRepeatingQuestAction()
    object QuestSaved : EditRepeatingQuestAction()
    data class SaveInvalidQuest(val error: RepeatingQuestSideEffectHandler.ValidationError) :
        EditRepeatingQuestAction()
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
                val dataState = state.dataState
                val rq = dataState.repeatingQuests.first { it.id == action.repeatingQuestId }

                subState.copy(
                    type = DATA_LOADED,
                    id = rq.id,
                    name = rq.name,
                    startTime = rq.startTime,
                    repeatingPattern = rq.repeatingPattern,
                    repeatType = rq.repeatingPattern.repeatType,
                    duration = rq.duration,
                    reminder = rq.reminder,
                    icon = rq.icon,
                    color = rq.color
                )
            }

            is EditRepeatingQuestAction.ChangeRepeatingPattern -> {
                subState.copy(
                    type = REPEATING_PATTERN_CHANGED,
                    repeatingPattern = action.repeatingPattern,
                    repeatType = action.repeatingPattern.repeatType
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
                val r: ReminderViewModel? = action.reminder
                val reminder = if (subState.startTime == null || r == null) {
                    null
                } else {
                    Reminder(r.message, subState.startTime.minus(r.minutesFromStart.toInt()), null)
                }
                subState.copy(
                    type = REMINDER_CHANGED,
                    reminder = reminder
                )
            }

            is EditRepeatingQuestAction.ChangeName -> {
                subState.copy(
                    type = NAME_CHANGED,
                    name = action.name
                )
            }

            is EditRepeatingQuestAction.ChangeColor -> {
                subState.copy(
                    type = COLOR_CHANGED,
                    color = action.color
                )
            }

            is EditRepeatingQuestAction.ChangeIcon -> {
                subState.copy(
                    type = ICON_CHANGED,
                    icon = action.icon
                )
            }

            is EditRepeatingQuestAction.SaveInvalidQuest -> {
                subState.copy(
                    type = VALIDATION_ERROR_EMPTY_NAME
                )
            }

            EditRepeatingQuestAction.QuestSaved -> {
                subState.copy(
                    type = QUEST_SAVED
                )
            }

            else -> subState
        }

    override fun defaultState() =
        EditRepeatingQuestViewState(
            type = LOADING,
            id = "",
            name = "",
            repeatingPattern = RepeatingPattern.Daily(),
            repeatType = RepeatType.DAILY,
            duration = Constants.QUEST_MIN_DURATION,
            startTime = null,
            reminder = null,
            icon = null,
            color = Color.GREEN
        )

}


data class EditRepeatingQuestViewState(
    val type: EditRepeatingQuestViewState.StateType,
    val id: String,
    val name: String,
    val repeatingPattern: RepeatingPattern,
    val repeatType: RepeatType,
    val duration: Int,
    val startTime: Time?,
    val reminder: Reminder?,
    val icon: Icon?,
    val color: Color
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        REPEATING_PATTERN_CHANGED,
        START_TIME_CHANGED,
        DURATION_CHANGED,
        REMINDER_CHANGED,
        NAME_CHANGED,
        COLOR_CHANGED,
        ICON_CHANGED,
        VALIDATION_ERROR_EMPTY_NAME,
        QUEST_SAVED
    }
}

val EditRepeatingQuestViewState.reminderViewModel: ReminderViewModel?
    get() = reminder?.let {
        if (startTime == null) {
            null
        } else {
            ReminderViewModel(
                it.message,
                it.toMinutesFromStart(startTime).toLong()
            )
        }
    }