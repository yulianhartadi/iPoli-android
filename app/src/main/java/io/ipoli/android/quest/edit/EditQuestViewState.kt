package io.ipoli.android.quest.edit

import io.ipoli.android.Constants
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.Validator
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.edit.EditQuestViewState.StateType.*
import io.ipoli.android.quest.reminder.picker.ReminderViewModel
import io.ipoli.android.quest.subquest.SubQuest
import io.ipoli.android.quest.toMinutesFromStart
import io.ipoli.android.tag.Tag
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 4/10/18.
 */
sealed class EditQuestAction : Action {

    data class StartAdd(val params: EditQuestViewController.Params?) : EditQuestAction()
    data class Load(
        val questId: String,
        val params: EditQuestViewController.Params?
    ) : EditQuestAction()

    data class Loaded(val quest: Quest, val params: EditQuestViewController.Params?) :
        EditQuestAction()

    data class ChangeColor(val color: Color) : EditQuestAction()
    data class ChangeIcon(val icon: Icon?) : EditQuestAction()
    data class ChangeDate(val scheduleDate: LocalDate) : EditQuestAction()
    data class ChangeDuration(val duration: Int) : EditQuestAction()
    data class ChangeStartTime(val time: Time?) : EditQuestAction()
    data class ChangeNote(val note: String) : EditQuestAction()
    data class ChangeReminder(val reminder: ReminderViewModel?) : EditQuestAction()
    data class ChangeChallenge(val challenge: Challenge?) : EditQuestAction()
    data class Validate(val name: String) : EditQuestAction()
    data class AddSubQuest(val name: String) : EditQuestAction()

    data class Save(val newSubQuestNames: Map<String, String>) : EditQuestAction()
    data class RemoveTag(val tag: Tag) : EditQuestAction()
    data class AddTag(val tagName: String) : EditQuestAction()
}

object EditQuestReducer : BaseViewStateReducer<EditQuestViewState>() {

    override val stateKey = key<EditQuestViewState>()

    override fun reduce(
        state: AppState,
        subState: EditQuestViewState,
        action: Action
    ) = when (action) {

        is EditQuestAction.StartAdd -> {
            val params = action.params
            subState.copy(
                type = DATA_LOADED,
                name = params?.name ?: subState.name,
                scheduleDate = params?.scheduleDate ?: subState.scheduleDate,
                startTime = params?.startTime ?: subState.startTime,
                duration = params?.duration ?: subState.duration,
                icon = params?.icon ?: subState.icon,
                color = params?.color ?: subState.color,
                reminder = params?.reminderViewModel ?: subState.reminder,
                tags = state.dataState.tags
            )
        }

        is EditQuestAction.Loaded -> {
            val quest = action.quest
            val params = action.params
            val reminderViewModel = quest.reminders.firstOrNull()?.let {
                if (quest.startTime == null) {
                    null
                } else {
                    ReminderViewModel(
                        it.message,
                        it.toMinutesFromStart(quest.startTime).toLong()
                    )
                }
            }
            val challenge = if (quest.isFromChallenge) {
                state.dataState.challenges.first { it.id == quest.challengeId }
            } else null

            val subQuests = quest.subQuests.map {
                UUID.randomUUID().toString() to it
            }.toMap()
            subState.copy(
                type = DATA_LOADED,
                id = quest.id,
                name = params?.name ?: quest.name,
                scheduleDate = params?.scheduleDate ?: quest.scheduledDate,
                startTime = params?.startTime ?: quest.startTime,
                duration = params?.duration ?: quest.duration,
                reminder = params?.reminderViewModel ?: reminderViewModel,
                color = params?.color ?: quest.color,
                icon = params?.icon ?: quest.icon,
                challenge = challenge,
                note = quest.note,
                repeatingQuestId = quest.repeatingQuestId,
                subQuests = subQuests,
                questTags = quest.tags,
                tags = state.dataState.tags - quest.tags,
                maxTagsReached = quest.tags.size >= Constants.MAX_TAGS_PER_ITEM
            )
        }

        is DataLoadedAction.TagsChanged -> {
            subState.copy(
                type = TAGS_CHANGED,
                tags = action.tags
            )
        }

        is EditQuestAction.RemoveTag -> {
            val questTags = subState.questTags - action.tag
            subState.copy(
                type = TAGS_CHANGED,
                questTags = questTags,
                tags = subState.tags + action.tag,
                maxTagsReached = questTags.size >= Constants.MAX_TAGS_PER_ITEM
            )
        }

        is EditQuestAction.AddTag -> {
            val tag = subState.tags.first { it.name == action.tagName }
            val questTags = subState.questTags + tag
            subState.copy(
                type = TAGS_CHANGED,
                questTags = questTags,
                tags = subState.tags - tag,
                maxTagsReached = questTags.size >= Constants.MAX_TAGS_PER_ITEM
            )
        }

        is EditQuestAction.ChangeDate -> {
            subState.copy(
                type = SCHEDULE_DATE_CHANGED,
                scheduleDate = action.scheduleDate
            )
        }

        is EditQuestAction.ChangeDuration -> {
            subState.copy(
                type = DURATION_CHANGED,
                duration = action.duration
            )
        }

        is EditQuestAction.ChangeStartTime -> {
            subState.copy(
                type = START_TIME_CHANGED,
                startTime = action.time
            )
        }

        is EditQuestAction.ChangeIcon -> {
            subState.copy(
                type = ICON_CHANGED,
                icon = action.icon
            )
        }

        is EditQuestAction.ChangeColor -> {
            subState.copy(
                type = COLOR_CHANGED,
                color = action.color
            )
        }

        is EditQuestAction.ChangeReminder -> {
            subState.copy(
                type = REMINDER_CHANGED,
                reminder = action.reminder
            )
        }

        is EditQuestAction.ChangeNote ->
            subState.copy(
                type = NOTE_CHANGED,
                note = action.note.trim()
            )

        is EditQuestAction.ChangeChallenge -> {
            subState.copy(
                type = CHALLENGE_CHANGED,
                challenge = action.challenge
            )
        }

        is EditQuestAction.AddSubQuest -> {
            subState.copy(
                type = SUB_QUEST_ADDED,
                newSubQuestName = action.name
            )
        }


        is EditQuestAction.Validate -> {
            val errors = Validator.validate(action).check<ValidationError> {
                "name" {
                    given { name.isEmpty() } addError ValidationError.EMPTY_NAME
                }
            }
            subState.copy(
                type = if (errors.isEmpty()) {
                    VALIDATION_SUCCESSFUL
                } else {
                    VALIDATION_ERROR_EMPTY_NAME
                },
                name = action.name
            )
        }
        else -> subState
    }

    override fun defaultState() =
        EditQuestViewState(
            type = LOADING,
            id = "",
            name = "",
            scheduleDate = LocalDate.now(),
            startTime = null,
            duration = Constants.QUEST_MIN_DURATION,
            reminder = null,
            color = Color.GREEN,
            icon = null,
            challenge = null,
            note = "",
            repeatingQuestId = null,
            subQuests = emptyMap(),
            newSubQuestName = "",
            tags = emptyList(),
            questTags = emptyList(),
            maxTagsReached = false
        )

    enum class ValidationError {
        EMPTY_NAME
    }
}

data class EditQuestViewState(
    val type: EditQuestViewState.StateType,
    val id: String,
    val name: String,
    val scheduleDate: LocalDate,
    val startTime: Time?,
    val duration: Int,
    val reminder: ReminderViewModel?,
    val color: Color,
    val icon: Icon?,
    val challenge: Challenge?,
    val note: String,
    val repeatingQuestId: String?,
    val subQuests: Map<String, SubQuest>,
    val newSubQuestName: String,
    val tags: List<Tag>,
    val questTags: List<Tag>,
    val maxTagsReached: Boolean
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        SCHEDULE_DATE_CHANGED,
        START_TIME_CHANGED,
        DURATION_CHANGED,
        COLOR_CHANGED,
        ICON_CHANGED,
        REMINDER_CHANGED,
        CHALLENGE_CHANGED,
        NOTE_CHANGED,
        VALIDATION_ERROR_EMPTY_NAME,
        VALIDATION_SUCCESSFUL,
        SUB_QUEST_ADDED,
        TAGS_CHANGED
    }
}