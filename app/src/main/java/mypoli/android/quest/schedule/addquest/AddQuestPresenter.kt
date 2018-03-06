package mypoli.android.quest.schedule.addquest

import mypoli.android.Constants
import mypoli.android.common.Validator
import mypoli.android.common.datetime.Time
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.quest.Category
import mypoli.android.quest.Color
import mypoli.android.quest.Reminder
import mypoli.android.quest.schedule.addquest.StateType.*
import mypoli.android.quest.usecase.Result
import mypoli.android.quest.usecase.SaveQuestUseCase
import mypoli.android.quest.reminder.picker.ReminderViewModel
import mypoli.android.repeatingquest.sideeffect.RepeatingQuestSideEffect
import mypoli.android.repeatingquest.usecase.SaveRepeatingQuestUseCase
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
    private val saveRepeatingQuestUseCase: SaveRepeatingQuestUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<AddQuestViewState>, AddQuestViewState, AddQuestIntent>(
    AddQuestViewState(
        type = DEFAULT,
        originalDate = LocalDate.now()
    ),
    coroutineContext
) {
    override fun reduceState(intent: AddQuestIntent, state: AddQuestViewState) =
        when (intent) {

            is AddQuestIntent.LoadData ->
                state.copy(
                    type = DEFAULT,
                    originalDate = intent.startDate,
                    date = intent.startDate
                )

            AddQuestIntent.PickDate ->
                state.copy(type = PICK_DATE, isRepeating = false)

            is AddQuestIntent.DatePicked -> {
                val date = LocalDate.of(intent.year, intent.month, intent.day)
                state.copy(type = DEFAULT, date = date, isRepeating = false)
            }

            AddQuestIntent.PickTime ->
                state.copy(type = PICK_TIME)

            is AddQuestIntent.TimePicked ->
                state.copy(type = DEFAULT, time = intent.time)

            AddQuestIntent.PickDuration ->
                state.copy(type = PICK_DURATION)

            is AddQuestIntent.DurationPicked ->
                state.copy(type = DEFAULT, duration = intent.minutes)

            AddQuestIntent.PickColor ->
                state.copy(type = PICK_COLOR)

            is AddQuestIntent.ColorPicked ->
                state.copy(type = DEFAULT, color = intent.color)

            AddQuestIntent.PickIcon ->
                state.copy(type = PICK_ICON)

            is AddQuestIntent.IconPicked ->
                state.copy(type = DEFAULT, icon = intent.icon)

            AddQuestIntent.PickReminder ->
                state.copy(type = PICK_REMINDER)

            is AddQuestIntent.ReminderPicked ->
                state.copy(type = DEFAULT, reminder = intent.reminder)

            AddQuestIntent.PickRepeatingPattern ->
                state.copy(type = PICK_REPEATING_PATTERN)

            is AddQuestIntent.RepeatingPatternPicked -> {
                state.copy(type = DEFAULT, repeatingPattern = intent.pattern, isRepeating = true)
            }

            AddQuestIntent.RepeatingPatterPickerCanceled -> {
                if (state.date != null) {
                    state.copy(type = SWITCHED_TO_QUEST, isRepeating = false)
                } else {
                    state.copy(type = DEFAULT)
                }
            }

            AddQuestIntent.DatePickerCanceled -> {
                if (state.repeatingPattern != null) {
                    state.copy(type = SWITCHED_TO_REPEATING, isRepeating = true)
                } else {
                    state.copy(type = DEFAULT)
                }
            }

            is AddQuestIntent.SaveQuest -> {
                val color = state.color ?: Color.GREEN
                val scheduledDate = state.date ?: LocalDate.now()

                val reminder = createReminder(state, scheduledDate)

                if (state.isRepeating) {
                    val rqParams = SaveRepeatingQuestUseCase.Params(
                        name = intent.name,
                        color = Color.valueOf(color.name),
                        icon = state.icon,
                        category = Category("WELLNESS", Color.GREEN),
                        startTime = state.time,
                        duration = state.duration ?: Constants.QUEST_MIN_DURATION,
                        reminder = reminder,
                        repeatingPattern = state.repeatingPattern!!
                    )


                    val errors = Validator.validate(rqParams)
                        .check<RepeatingQuestSideEffect.ValidationError> {
                            "name" {
                                given { name.isEmpty() } addError RepeatingQuestSideEffect.ValidationError.EMPTY_NAME
                            }
                        }

                    if (errors.isNotEmpty()) {
                        state.copy(type = VALIDATION_ERROR_EMPTY_NAME)
                    } else {
                        saveRepeatingQuestUseCase.execute(rqParams)
                        AddQuestViewState(
                            type = QUEST_SAVED,
                            originalDate = state.originalDate,
                            date = state.originalDate
                        )
                    }

                } else {

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
                        is Result.Added ->
                            AddQuestViewState(
                                type = QUEST_SAVED,
                                originalDate = state.originalDate,
                                date = state.originalDate
                            )
                    }
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

    private fun createReminderFromViewModel(
        reminder: ReminderViewModel?,
        scheduledDate: LocalDate,
        startMinute: Int
    ): Reminder? {
        return reminder?.let {
            val time = Time.of(startMinute)
            val questDateTime =
                LocalDateTime.of(scheduledDate, LocalTime.of(time.hours, time.getMinutes()))
            val reminderDateTime = questDateTime.minusMinutes(it.minutesFromStart)
            val toLocalTime = reminderDateTime.toLocalTime()
            Reminder(
                it.message,
                Time.at(toLocalTime.hour, toLocalTime.minute),
                reminderDateTime.toLocalDate()
            )
        }
    }

    private fun createDefaultReminder(scheduledDate: LocalDate, startMinute: Int) =
        Reminder("", Time.of(startMinute), scheduledDate)

}