package io.ipoli.android.quest.calendar.addquest

import io.ipoli.android.common.datetime.Time
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
        type = StateType.DEFAULT,
        name = ""
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

            is TimePickedIntent -> {
                val time = Time.at(intent.hour, intent.minute)
                state.copy(type = StateType.DEFAULT, time = time)
            }

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

            else -> {
                state
            }

        }

}