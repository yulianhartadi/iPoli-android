package io.ipoli.android.quest.schedule.addquest

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.reminder.picker.ReminderViewModel
import io.ipoli.android.quest.schedule.agenda.AgendaReducer
import io.ipoli.android.quest.usecase.Result
import io.ipoli.android.repeatingquest.entity.RepeatingPattern
import io.ipoli.android.repeatingquest.sideeffect.RepeatingQuestSideEffectHandler
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 11/2/17.
 */

sealed class AddQuestAction : Action {
    data class Save(val name: String, val subQuestNames: List<String>) : AddQuestAction()
    data class DatePicked(val date: LocalDate?) : AddQuestAction()
    object DatePickerCanceled : AddQuestAction()
    data class TimePicked(val time: Time?) : AddQuestAction()
    data class DurationPicked(val minutes: Int) : AddQuestAction()
    data class ColorPicked(val color: Color) : AddQuestAction()
    data class IconPicked(val icon: Icon?) : AddQuestAction()
    data class ReminderPicked(val reminder: ReminderViewModel?) : AddQuestAction()
    data class RepeatingPatternPicked(val pattern: RepeatingPattern) : AddQuestAction()
    object RepeatingPatterPickerCanceled : AddQuestAction()
    data class Load(val date: LocalDate) : AddQuestAction()
    object AddSubQuest : AddQuestAction()
    data class SaveRepeatingQuest(
        val name: String,
        val subQuestNames: List<String>
    ) : AddQuestAction()

    data class SaveInvalidRepeatingQuest(val error: RepeatingQuestSideEffectHandler.ValidationError) :
        AddQuestAction()

    object RepeatingQuestSaved : AddQuestAction()
    object QuestSaved : AddQuestAction()
    data class SaveInvalidQuest(val error: Result.ValidationError) : AddQuestAction()
    data class NotePicked(val note: String) : AddQuestAction()
}

object AddQuestReducer : BaseViewStateReducer<AddQuestViewState>() {

    override val stateKey = AgendaReducer.key<AddQuestViewState>()

    override fun reduce(
        state: AppState,
        subState: AddQuestViewState,
        action: Action
    ) =
        when (action) {
            is AddQuestAction.Load ->
                subState.copy(
                    type = StateType.DATA_LOADED,
                    originalDate = action.date,
                    date = action.date
                )

            is AddQuestAction.DatePicked -> {
                subState.copy(type = StateType.DATE_PICKED, date = action.date, isRepeating = false)
            }

            is AddQuestAction.RepeatingPatternPicked -> {
                subState.copy(
                    type = StateType.REPEATING_PATTERN_PICKED,
                    repeatingPattern = action.pattern,
                    isRepeating = true
                )
            }

            AddQuestAction.RepeatingPatterPickerCanceled -> {
                if (subState.date != null) {
                    subState.copy(type = StateType.SWITCHED_TO_QUEST, isRepeating = false)
                } else {
                    subState.copy(type = StateType.PICK_REPEATING_PATTERN_CANCELED)
                }
            }

            AddQuestAction.DatePickerCanceled -> {
                if (subState.repeatingPattern != null) {
                    subState.copy(type = StateType.SWITCHED_TO_REPEATING, isRepeating = true)
                } else {
                    subState.copy(type = StateType.PICK_DATE_CANCELED)
                }
            }

            is AddQuestAction.TimePicked ->
                subState.copy(type = StateType.TIME_PICKED, time = action.time)

            is AddQuestAction.DurationPicked ->
                subState.copy(type = StateType.DURATION_PICKED, duration = action.minutes)

            is AddQuestAction.ColorPicked ->
                subState.copy(type = StateType.COLOR_PICKED, color = action.color)

            is AddQuestAction.IconPicked ->
                subState.copy(type = StateType.ICON_PICKED, icon = action.icon)

            is AddQuestAction.ReminderPicked ->
                subState.copy(type = StateType.REMINDER_PICKED, reminder = action.reminder)

            is AddQuestAction.NotePicked -> {
                val note = action.note.trim()
                subState.copy(
                    type = StateType.NOTE_PICKED,
                    note = if (note.isEmpty()) null else note
                )
            }

            is AddQuestAction.SaveInvalidRepeatingQuest -> {
                subState.copy(type = StateType.VALIDATION_ERROR_EMPTY_NAME)
            }

            AddQuestAction.RepeatingQuestSaved -> {
                defaultState().copy(
                    type = StateType.QUEST_SAVED,
                    originalDate = subState.originalDate,
                    date = subState.date
                )
            }

            is AddQuestAction.SaveInvalidQuest -> {
                subState.copy(type = StateType.VALIDATION_ERROR_EMPTY_NAME)
            }

            AddQuestAction.AddSubQuest ->
                subState.copy(
                    type = StateType.ADD_SUB_QUEST
                )

            AddQuestAction.QuestSaved -> {
                defaultState().copy(
                    type = StateType.QUEST_SAVED,
                    originalDate = subState.originalDate,
                    date = subState.date
                )
            }

            else -> subState
        }

    override fun defaultState() =
        AddQuestViewState(
            type = StateType.DATA_LOADED,
            originalDate = LocalDate.now(),
            date = null,
            color = null,
            duration = null,
            time = null,
            icon = null,
            reminder = null,
            repeatingPattern = null,
            isRepeating = false,
            note = null
        )
}


data class AddQuestViewState(
    val type: StateType,
    val originalDate: LocalDate,
    val date: LocalDate?,
    val time: Time?,
    val duration: Int?,
    val color: Color?,
    val icon: Icon?,
    val reminder: ReminderViewModel?,
    val repeatingPattern: RepeatingPattern?,
    val note: String?,
    val isRepeating: Boolean
) : ViewState

enum class StateType {
    DATA_LOADED,
    DATE_PICKED,
    TIME_PICKED,
    DURATION_PICKED,
    COLOR_PICKED,
    ICON_PICKED,
    REMINDER_PICKED,
    REPEATING_PATTERN_PICKED,
    VALIDATION_ERROR_EMPTY_NAME,
    VALIDATION_ERROR_NO_REPEATING_PATTERN,
    QUEST_SAVED,
    SWITCHED_TO_QUEST,
    SWITCHED_TO_REPEATING,
    PICK_DATE_CANCELED,
    PICK_REPEATING_PATTERN_CANCELED,
    NOTE_PICKED,
    ADD_SUB_QUEST
}