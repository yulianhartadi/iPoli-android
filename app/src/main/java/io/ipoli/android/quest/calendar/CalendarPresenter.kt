package io.ipoli.android.quest.calendar

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/21/17.
 */
class CalendarPresenter(
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<CalendarViewState>, CalendarViewState, CalendarIntent>(coroutineContext) {

    override fun reduceState(intent: CalendarIntent, state: CalendarViewState): CalendarViewState {
        return state
    }

}