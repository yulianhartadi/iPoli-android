package io.ipoli.android.quest.calendar.addquest

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
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
        type = StateType.LOADING,
        name = ""
    ),
    coroutineContext
) {
    override fun reduceState(intent: AddQuestIntent, state: AddQuestViewState) =
        when (intent) {
            is PickDateIntent ->
                state.copy(type = StateType.SHOW_DATE_PICKER)

            is DatePickedIntent -> {
                val date = LocalDate.of(intent.year, intent.month, intent.day)
                state.copy(type = StateType.DEFAULT, date = date)
            }

            else -> {
                state
            }

        }

}