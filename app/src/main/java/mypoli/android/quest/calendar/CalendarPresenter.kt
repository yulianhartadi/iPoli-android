package mypoli.android.quest.calendar

import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.common.text.CalendarFormatter
import mypoli.android.player.ExperienceForLevelGenerator
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import mypoli.android.quest.calendar.CalendarViewState.DatePickerState.*
import mypoli.android.quest.calendar.CalendarViewState.StateType.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/21/17.
 */
class CalendarPresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    private val calendarFormatter: CalendarFormatter,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<CalendarViewState>, CalendarViewState, CalendarIntent>(
    CalendarViewState(
        type = LOADING,
        currentDate = LocalDate.now(),
        datePickerState = INVISIBLE,
        adapterPosition = MID_POSITION
    ),
    coroutineContext
) {

    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM")

    override fun reduceState(intent: CalendarIntent, state: CalendarViewState): CalendarViewState =
        when (intent) {
            is CalendarIntent.LoadData -> {
                launch {
                    listenForPlayerChangesUseCase.listen(Unit).consumeEach {
                        sendChannel.send(CalendarIntent.ChangePlayer(it))
                    }
                }
                val date = intent.currentDate
                val (dayText, dateText) = formatDayAndDate(date)
                state.copy(
                    type = DATA_LOADED,
                    adapterPosition = MID_POSITION,
                    currentDate = intent.currentDate,
                    dayText = dayText,
                    monthText = monthFormatter.format(date),
                    dateText = dateText
                )
            }
            is CalendarIntent.ExpandToolbar -> {
                when (state.datePickerState) {
                    INVISIBLE -> state.copy(type = DATE_PICKER_CHANGED, datePickerState = SHOW_WEEK)
                    else -> state.copy(type = DATE_PICKER_CHANGED, datePickerState = INVISIBLE)
                }
            }
            is CalendarIntent.ExpandToolbarWeek -> {
                when (state.datePickerState) {
                    SHOW_WEEK -> state.copy(
                        type = DATE_PICKER_CHANGED,
                        datePickerState = SHOW_MONTH
                    )
                    else -> state.copy(type = DATE_PICKER_CHANGED, datePickerState = SHOW_WEEK)
                }
            }
            is CalendarIntent.CalendarChangeDate -> {
                val newDate = LocalDate.of(intent.year, intent.month, intent.day)
                val (dayText, dateText) = formatDayAndDate(newDate)
                state.copy(
                    type = CALENDAR_DATE_CHANGED,
                    currentDate = newDate,
                    dayText = dayText,
                    monthText = monthFormatter.format(newDate),
                    dateText = dateText,
                    adapterPosition = MID_POSITION
                )
            }
            is CalendarIntent.SwipeChangeDate -> {
                val newDate =
                    state.currentDate.plusDays((intent.position - state.adapterPosition).toLong())
                val (dayText, dateText) = formatDayAndDate(newDate)
                state.copy(
                    type = SWIPE_DATE_CHANGED,
                    currentDate = newDate,
                    dayText = dayText,
                    monthText = monthFormatter.format(newDate),
                    dateText = dateText,
                    adapterPosition = intent.position
                )
            }
            is CalendarIntent.ChangeMonth -> {
                val newDate = LocalDate.of(intent.year, intent.month, 1)
                state.copy(
                    type = DEFAULT,
                    monthText = monthFormatter.format(newDate)
                )
            }

            is CalendarIntent.ChangePlayer -> {
                val player = intent.player

                val type = when {
                    state.level == 0 -> PLAYER_LOADED
                    state.level != player.level -> LEVEL_CHANGED
                    else -> XP_AND_COINS_CHANGED
                }

                val thisLevelXP = ExperienceForLevelGenerator.forLevel(player.level).toInt()
                val nextLevelXP = ExperienceForLevelGenerator.forLevel(player.level + 1).toInt()

                state.copy(
                    type = type,
                    level = player.level,
                    progress = player.experience.toInt() - thisLevelXP,
                    coins = player.coins,
                    maxProgress = nextLevelXP - thisLevelXP
                )
            }
        }

    private fun formatDayAndDate(date: LocalDate): Pair<String, String> {
        val dayText = calendarFormatter.day(date)
        val dateText = calendarFormatter.date(date)
        return Pair(dayText, dateText)
    }

    companion object {
        const val MID_POSITION = 49
    }
}