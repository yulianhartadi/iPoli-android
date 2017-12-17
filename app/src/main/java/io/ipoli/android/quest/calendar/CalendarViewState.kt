package io.ipoli.android.quest.calendar

import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.player.Player
import io.ipoli.android.quest.calendar.CalendarViewState.StateType.DATA_LOADED
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/21/17.
 */

sealed class CalendarIntent : Intent {
    data class LoadData(val currentDate: LocalDate) : CalendarIntent()
    data class SwipeChangeDate(val position: Int) : CalendarIntent()
    data class CalendarChangeDate(val year: Int, val month: Int, val day: Int) : CalendarIntent()
    data class ChangeMonth(val year: Int, val month: Int) : CalendarIntent()
    object ExpandToolbar : CalendarIntent()
    object ExpandToolbarWeek : CalendarIntent()
    data class ChangePlayer(val player: Player) : CalendarIntent()
}

data class CalendarViewState(
    val type: StateType = DATA_LOADED,
    val currentDate: LocalDate,
    val monthText: String = "",
    val dayText: String = "",
    val dateText: String = "",
    val datePickerState: DatePickerState,
    val adapterPosition: Int,
    val progress: Int = 0,
    val maxProgress: Int = 0,
    val level: Int = 0,
    val coins: Int = 0
) : ViewState {

    enum class StateType {
        LOADING, DATA_LOADED, CALENDAR_DATE_CHANGED, SWIPE_DATE_CHANGED, DEFAULT, DATE_PICKER_CHANGED,
        LEVEL_CHANGED, XP_AND_COINS_CHANGED, PLAYER_LOADED
    }

    enum class DatePickerState {
        INVISIBLE, SHOW_WEEK, SHOW_MONTH
    }
}