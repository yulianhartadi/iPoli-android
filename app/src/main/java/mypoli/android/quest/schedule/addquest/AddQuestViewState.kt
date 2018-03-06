package mypoli.android.quest.schedule.addquest

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.datetime.Time
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.quest.Color
import mypoli.android.quest.Icon
import mypoli.android.quest.schedule.agenda.AgendaReducer
import mypoli.android.quest.usecase.Result
import mypoli.android.reminder.view.picker.ReminderViewModel
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.sideeffect.RepeatingQuestSideEffect
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 11/2/17.
 */
sealed class AddQuestIntent : Intent {
    data class LoadData(val startDate: LocalDate) : AddQuestIntent()
    object PickDate : AddQuestIntent()
    object PickTime : AddQuestIntent()
    object PickDuration : AddQuestIntent()
    object PickColor : AddQuestIntent()
    object PickReminder : AddQuestIntent()
    object PickIcon : AddQuestIntent()
    object PickRepeatingPattern : AddQuestIntent()
    data class DatePicked(val year: Int, val month: Int, val day: Int) : AddQuestIntent()
    data class TimePicked(val time: Time?) : AddQuestIntent()
    data class DurationPicked(val minutes: Int) : AddQuestIntent()
    data class ColorPicked(val color: Color) : AddQuestIntent()
    data class IconPicked(val icon: Icon?) : AddQuestIntent()
    data class ReminderPicked(val reminder: ReminderViewModel?) : AddQuestIntent()
    data class RepeatingPatternPicked(val pattern: RepeatingPattern) : AddQuestIntent()
    data class SaveQuest(val name: String) : AddQuestIntent()
    object RepeatingPatterPickerCanceled : AddQuestIntent()
    object DatePickerCanceled : AddQuestIntent()
}

sealed class AddQuestAction : Action {
    object PickDate : AddQuestAction()
    object PickTime : AddQuestAction()
    object PickDuration : AddQuestAction()
    object PickColor : AddQuestAction()
    object PickIcon : AddQuestAction()
    object PickReminder : AddQuestAction()
    object PickRepeatingPattern : AddQuestAction()
    data class Save(val name: String) : AddQuestAction()
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
    data class SaveRepeatingQuest(val name: String) : AddQuestAction()
    data class SaveInvalidRepeatingQuest(val error: RepeatingQuestSideEffect.ValidationError) :
        AddQuestAction()

    object RepeatingQuestSaved : AddQuestAction()
    object QuestSaved : AddQuestAction()
    data class SaveInvalidQuest(val error: Result.ValidationError) : AddQuestAction()

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
            AddQuestAction.PickDate ->
                subState.copy(type = StateType.PICK_DATE, isRepeating = false)

            is AddQuestAction.DatePicked -> {
                subState.copy(type = StateType.DATA_LOADED, date = action.date, isRepeating = false)
            }

            AddQuestAction.PickTime ->
                subState.copy(type = StateType.PICK_TIME)

            is AddQuestAction.TimePicked ->
                subState.copy(type = StateType.DATA_LOADED, time = action.time)

            AddQuestAction.PickDuration ->
                subState.copy(type = StateType.PICK_DURATION)

            is AddQuestAction.DurationPicked ->
                subState.copy(type = StateType.DATA_LOADED, duration = action.minutes)

            AddQuestAction.PickColor ->
                subState.copy(type = StateType.PICK_COLOR)

            is AddQuestAction.ColorPicked ->
                subState.copy(type = StateType.DATA_LOADED, color = action.color)

            AddQuestAction.PickIcon ->
                subState.copy(type = StateType.PICK_ICON)

            is AddQuestAction.IconPicked ->
                subState.copy(type = StateType.DATA_LOADED, icon = action.icon)

            AddQuestAction.PickReminder ->
                subState.copy(type = StateType.PICK_REMINDER)

            is AddQuestAction.ReminderPicked ->
                subState.copy(type = StateType.DATA_LOADED, reminder = action.reminder)

            AddQuestAction.PickRepeatingPattern ->
                subState.copy(type = StateType.PICK_REPEATING_PATTERN)

            is AddQuestAction.RepeatingPatternPicked -> {
                subState.copy(
                    type = StateType.DATA_LOADED,
                    repeatingPattern = action.pattern,
                    isRepeating = true
                )
            }

            AddQuestAction.RepeatingPatterPickerCanceled -> {
                if (subState.date != null) {
                    subState.copy(type = StateType.SWITCHED_TO_QUEST, isRepeating = false)
                } else {
                    subState.copy(type = StateType.DATA_LOADED)
                }
            }

            AddQuestAction.DatePickerCanceled -> {
                if (subState.repeatingPattern != null) {
                    subState.copy(type = StateType.SWITCHED_TO_REPEATING, isRepeating = true)
                } else {
                    subState.copy(type = StateType.DATA_LOADED)
                }
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
            isRepeating = false
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
    val isRepeating: Boolean
) : ViewState

enum class StateType {
    DATA_LOADED,
    PICK_DATE,
    PICK_TIME,
    PICK_DURATION,
    PICK_COLOR,
    PICK_ICON,
    PICK_REMINDER,
    PICK_REPEATING_PATTERN,
    VALIDATION_ERROR_EMPTY_NAME,
    VALIDATION_ERROR_NO_REPEATING_PATTERN,
    QUEST_SAVED,
    SWITCHED_TO_QUEST,
    SWITCHED_TO_REPEATING
}