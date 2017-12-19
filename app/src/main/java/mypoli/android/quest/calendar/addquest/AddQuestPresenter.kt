package mypoli.android.quest.calendar.addquest

import mypoli.android.Constants
import mypoli.android.common.datetime.Time
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.quest.Category
import mypoli.android.quest.Color
import mypoli.android.quest.Reminder
import mypoli.android.quest.calendar.addquest.StateType.*
import mypoli.android.quest.usecase.Result
import mypoli.android.quest.usecase.SaveQuestUseCase
import mypoli.android.reminder.view.picker.ReminderViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 11/2/17.
 */
class AddQuestPresenter(
    private val saveQuestUseCase: SaveQuestUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<AddQuestViewState>, AddQuestViewState, AddQuestIntent>(
    AddQuestViewState(
        type = DEFAULT
    ),
    coroutineContext
) {
    override fun reduceState(intent: AddQuestIntent, state: AddQuestViewState) =
        when (intent) {

            is AddQuestIntent.LoadData ->
                state.copy(
                    type = DEFAULT,
                    date = intent.startDate
                )

            is AddQuestIntent.PickDate ->
                state.copy(type = PICK_DATE)

            is AddQuestIntent.DatePicked -> {
                val date = LocalDate.of(intent.year, intent.month, intent.day)
                state.copy(type = DEFAULT, date = date)
            }

            is AddQuestIntent.PickTime ->
                state.copy(type = PICK_TIME)

            is AddQuestIntent.TimePicked ->
                state.copy(type = DEFAULT, time = intent.time)

            is AddQuestIntent.PickDuration ->
                state.copy(type = PICK_DURATION)

            is AddQuestIntent.DurationPicked ->
                state.copy(type = DEFAULT, duration = intent.minutes)

            is AddQuestIntent.PickColor ->
                state.copy(type = PICK_COLOR)

            is AddQuestIntent.ColorPicked ->
                state.copy(type = DEFAULT, color = intent.color)

            is AddQuestIntent.PickIcon ->
                state.copy(type = PICK_ICON)

            is AddQuestIntent.IconPicked ->
                state.copy(type = DEFAULT, icon = intent.icon)

            is AddQuestIntent.PickReminder ->
                state.copy(type = PICK_REMINDER)

            is AddQuestIntent.ReminderPicked ->
                state.copy(type = DEFAULT, reminder = intent.reminder)

            is AddQuestIntent.SaveQuest -> {
                val color = state.color ?: Color.GREEN
                val scheduledDate = state.date ?: LocalDate.now()

                val reminder = createReminder(state, scheduledDate)

                val questParams = SaveQuestUseCase.Parameters(
                    name = intent.name,
                    color = Color.valueOf(color.name),
                    icon = state.icon,
                    category = Category("WELLNESS", Color.GREEN),
                    scheduledDate = scheduledDate,
                    startTime = state.time,
                    duration = state.duration ?: Constants.QUEST_MIN_DURATION,
                    reminder = reminder
                )
                val result = saveQuestUseCase.execute(questParams)
                when (result) {
                    is Result.Invalid ->
                        state.copy(type = VALIDATION_ERROR_EMPTY_NAME)
                    else -> AddQuestViewState(type = QUEST_SAVED)
                }
            }
        }

    private fun createReminder(state: AddQuestViewState, scheduledDate: LocalDate) =
        state.time?.let {
            if (state.reminder != null) {
                createReminderFromViewModel(state.reminder, scheduledDate, it.toMinuteOfDay())
            } else {
                createDefaultReminder(scheduledDate, it.toMinuteOfDay())
            }
        }

    private fun createReminderFromViewModel(reminder: ReminderViewModel?, scheduledDate: LocalDate, startMinute: Int): Reminder? {
        return reminder?.let {
            val time = Time.of(startMinute)
            val questDateTime = LocalDateTime.of(scheduledDate, LocalTime.of(time.hours, time.getMinutes()))
            val reminderDateTime = questDateTime.minusMinutes(it.minutesFromStart)
            val toLocalTime = reminderDateTime.toLocalTime()
            Reminder(it.message, Time.at(toLocalTime.hour, toLocalTime.minute), reminderDateTime.toLocalDate())
        }
    }

    private fun createDefaultReminder(scheduledDate: LocalDate, startMinute: Int) =
        Reminder("", Time.of(startMinute), scheduledDate)

}