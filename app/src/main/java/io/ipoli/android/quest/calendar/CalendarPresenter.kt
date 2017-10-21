package io.ipoli.android.quest.calendar

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import org.threeten.bp.LocalDate
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/21/17.
 */
class CalendarPresenter(
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<CalendarViewState>, CalendarViewState, CalendarIntent>(
    coroutineContext, CalendarViewState(currentDate = LocalDate.now(), dayText = "", dateText = "")) {

    override fun reduceState(intent: CalendarIntent, state: CalendarViewState): CalendarViewState =
        when (intent) {
            is LoadDataIntent -> {
                val date = intent.currentDate
                val dayText = "Today"
                val dateText = "Sept 8th 17"
                state.copy(
                    currentDate = intent.currentDate,
                    dayText = dayText,
                    dateText = dateText)
            }
            else -> {
                state
            }
        }
}