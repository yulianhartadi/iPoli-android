package io.ipoli.android.quest.calendar

import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/21/17.
 */

sealed class CalendarIntent : Intent

data class LoadDataIntent(val currentDate: LocalDate) : CalendarIntent()
data class SwipeChangeDateIntent(val position: Int) : CalendarIntent()
object ExpandToolbarIntent : CalendarIntent()

data class CalendarViewState(
    val currentDate: LocalDate,
    val dayText: String = "",
    val dateText: String = "",
    val toolbarState: ToolbarState
) : ViewState {
    enum class ToolbarState {
        SHRINKED, SHOW_WEEK, SHOW_MONTH
    }
}