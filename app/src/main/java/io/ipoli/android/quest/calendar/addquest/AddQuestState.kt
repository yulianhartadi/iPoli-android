package io.ipoli.android.quest.calendar.addquest

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.reminder.view.picker.ReminderViewModel
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 11/2/17.
 */
sealed class AddQuestIntent : Intent

object PickDateIntent : AddQuestIntent()
object PickTimeIntent : AddQuestIntent()
object PickDurationIntent : AddQuestIntent()
object PickColorIntent : AddQuestIntent()
object PickReminderIntent : AddQuestIntent()
data class DatePickedIntent(val year: Int, val month: Int, val day: Int) : AddQuestIntent()
data class TimePickedIntent(val time: Time?) : AddQuestIntent()
data class DurationPickedIntent(val minutes: Int) : AddQuestIntent()
data class ColorPickedIntent(val color: AndroidColor) : AddQuestIntent()
data class ReminderPickedIntent(val reminder: ReminderViewModel?) : AddQuestIntent()
data class SaveQuestIntent(val name : String) : AddQuestIntent()

data class AddQuestViewState(
    val type: StateType,
    val date: LocalDate? = null,
    val time: Time? = null,
    val duration: Int? = null,
    val color: AndroidColor? = null,
    val reminder: ReminderViewModel? = null

) : ViewState

enum class StateType {
    DEFAULT,
    PICK_DATE,
    PICK_TIME,
    PICK_DURATION,
    PICK_COLOR,
    PICK_REMINDER,
    VALIDATION_ERROR_EMPTY_NAME,
    QUEST_SAVED
}