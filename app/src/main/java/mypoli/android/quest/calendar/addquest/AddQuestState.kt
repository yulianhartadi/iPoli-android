package mypoli.android.quest.calendar.addquest

import mypoli.android.common.datetime.Time
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.quest.Color
import mypoli.android.quest.Icon
import mypoli.android.reminder.view.picker.ReminderViewModel
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
    data class DatePicked(val year: Int, val month: Int, val day: Int) : AddQuestIntent()
    data class TimePicked(val time: Time?) : AddQuestIntent()
    data class DurationPicked(val minutes: Int) : AddQuestIntent()
    data class ColorPicked(val color: Color) : AddQuestIntent()
    data class IconPicked(val icon: Icon?) : AddQuestIntent()
    data class ReminderPicked(val reminder: ReminderViewModel?) : AddQuestIntent()
    data class SaveQuest(val name: String) : AddQuestIntent()
}

data class AddQuestViewState(
    val type: StateType,
    val date: LocalDate? = null,
    val time: Time? = null,
    val duration: Int? = null,
    val color: Color? = null,
    val icon: Icon? = null,
    val reminder: ReminderViewModel? = null

) : ViewState

enum class StateType {
    DEFAULT,
    PICK_DATE,
    PICK_TIME,
    PICK_DURATION,
    PICK_COLOR,
    PICK_ICON,
    PICK_REMINDER,
    VALIDATION_ERROR_EMPTY_NAME,
    QUEST_SAVED
}