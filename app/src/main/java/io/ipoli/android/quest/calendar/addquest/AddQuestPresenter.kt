package io.ipoli.android.quest.calendar.addquest

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.text.CalendarFormatter
import io.ipoli.android.quest.calendar.CalendarIntent
import io.ipoli.android.quest.calendar.CalendarPresenter
import io.ipoli.android.quest.calendar.CalendarViewState
import org.threeten.bp.LocalDate
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 11/2/17.
 */
class AddQuestPresenter(
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<AddQuestViewState>, AddQuestViewState, AddQuestIntent>(
    AddQuestViewState(
        name = ""
    ),
    coroutineContext
) {
    override fun reduceState(intent: AddQuestIntent, state: AddQuestViewState): AddQuestViewState {
        return state
    }

}