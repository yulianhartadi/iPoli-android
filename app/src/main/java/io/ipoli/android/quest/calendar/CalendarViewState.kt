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

data class CalendarViewState(
    val currentDate: LocalDate,
    val dayText: String,
    val dateText: String
) : ViewState