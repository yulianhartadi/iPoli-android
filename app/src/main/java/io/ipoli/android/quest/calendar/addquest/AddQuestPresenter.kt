package io.ipoli.android.quest.calendar.addquest

import io.ipoli.android.Constants
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.quest.Category
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Reminder
import io.ipoli.android.quest.usecase.Result
import io.ipoli.android.quest.usecase.SaveQuestUseCase
import io.ipoli.android.reminder.view.picker.ReminderViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 11/2/17.
 */
class AddQuestPresenter(
    private val saveQuestUseCase: SaveQuestUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<AddQuestViewState>, AddQuestViewState, AddQuestIntent>(
    AddQuestViewState(
        type = StateType.DEFAULT
    ),
    coroutineContext
) {
    override fun reduceState(intent: AddQuestIntent, state: AddQuestViewState) =
        when (intent) {
            is PickDateIntent ->
                state.copy(type = StateType.PICK_DATE)

            is DatePickedIntent -> {
                val date = LocalDate.of(intent.year, intent.month, intent.day)
                state.copy(type = StateType.DEFAULT, date = date)
            }

            is PickTimeIntent ->
                state.copy(type = StateType.PICK_TIME)

            is TimePickedIntent ->
                state.copy(type = StateType.DEFAULT, time = intent.time)

            is PickDurationIntent ->
                state.copy(type = StateType.PICK_DURATION)

            is DurationPickedIntent ->
                state.copy(type = StateType.DEFAULT, duration = intent.minutes)

            is PickColorIntent ->
                state.copy(type = StateType.PICK_COLOR)

            is ColorPickedIntent ->
                state.copy(type = StateType.DEFAULT, color = intent.color)

            is PickReminderIntent ->
                state.copy(type = StateType.PICK_REMINDER)

            is ReminderPickedIntent ->
                state.copy(type = StateType.DEFAULT, reminder = intent.reminder)

            is SaveQuestIntent -> {
                val color = state.color ?: Color.GREEN
                val scheduledDate = state.date ?: LocalDate.now()

                val reminder = state.time?.let {
                    createQuestReminder(state.reminder, scheduledDate, state.time.toMinuteOfDay())
                }

                val questParams = SaveQuestUseCase.Parameters(
                    name = intent.name,
                    color = Color.valueOf(color.name),
                    category = Category("WELLNESS", Color.GREEN),
                    scheduledDate = scheduledDate,
                    startTime = state.time,
                    duration = state.duration ?: Constants.QUEST_MIN_DURATION,
                    reminder = reminder
                )
                val result = saveQuestUseCase.execute(questParams)
                when (result) {
                    is Result.Invalid ->
                        state.copy(type = StateType.VALIDATION_ERROR_EMPTY_NAME)
                    else -> AddQuestViewState(type = StateType.QUEST_SAVED)
                }
            }

            else -> {
                state
            }

        }

    private fun createQuestReminder(reminder: ReminderViewModel?, scheduledDate: LocalDate, startMinute: Int): Reminder? {
        return reminder?.let {
            val time = Time.of(startMinute)
            val questDateTime = LocalDateTime.of(scheduledDate, LocalTime.of(time.hours, time.getMinutes()))
            val reminderDateTime = questDateTime.minusMinutes(it.minutesFromStart)
            val toLocalTime = reminderDateTime.toLocalTime()
            Reminder(it.message, Time.at(toLocalTime.hour, toLocalTime.minute), reminderDateTime.toLocalDate())
        }
    }

}