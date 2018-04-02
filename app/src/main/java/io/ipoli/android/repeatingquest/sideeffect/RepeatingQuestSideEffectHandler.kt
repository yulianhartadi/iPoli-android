package io.ipoli.android.repeatingquest.sideeffect

import io.ipoli.android.Constants
import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.Validator
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.Category
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Reminder
import io.ipoli.android.quest.reminder.picker.ReminderViewModel
import io.ipoli.android.quest.schedule.addquest.AddQuestAction
import io.ipoli.android.quest.schedule.addquest.AddQuestViewState
import io.ipoli.android.repeatingquest.edit.EditRepeatingQuestAction
import io.ipoli.android.repeatingquest.edit.EditRepeatingQuestViewState
import io.ipoli.android.repeatingquest.show.RepeatingQuestAction
import io.ipoli.android.repeatingquest.usecase.CreateRepeatingQuestHistoryUseCase
import io.ipoli.android.repeatingquest.usecase.RemoveRepeatingQuestUseCase
import io.ipoli.android.repeatingquest.usecase.SaveRepeatingQuestUseCase
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/03/2018.
 */
class RepeatingQuestSideEffectHandler : AppSideEffectHandler() {

    private val saveRepeatingQuestUseCase by required { saveRepeatingQuestUseCase }
    private val removeRepeatingQuestUseCase by required { removeRepeatingQuestUseCase }
    private val createRepeatingQuestHistoryUseCase by required { createRepeatingQuestHistoryUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is RepeatingQuestAction.Load -> {
                val today = LocalDate.now()
                val history = createRepeatingQuestHistoryUseCase.execute(
                    CreateRepeatingQuestHistoryUseCase.Params(
                        action.repeatingQuestId,
                        today.minusWeeks(3).with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek)),
                        today.plusWeeks(1).with(TemporalAdjusters.nextOrSame(DateUtils.lastDayOfWeek))
                    )
                )

                dispatch(
                    DataLoadedAction.RepeatingQuestHistoryChanged(
                        action.repeatingQuestId,
                        history
                    )
                )
            }

            is AddQuestAction.SaveRepeatingQuest -> {
                val rqState = state.stateFor(AddQuestViewState::class.java)
                val r: ReminderViewModel? = rqState.reminder
                val reminder = if (rqState.time == null || r == null) {
                    null
                } else {
                    Reminder(r.message, rqState.time.minus(r.minutesFromStart.toInt()), null)
                }
                val rqParams = SaveRepeatingQuestUseCase.Params(
                    name = action.name,
                    subQuestNames = action.subQuestNames,
                    color = rqState.color ?: Color.GREEN,
                    icon = rqState.icon,
                    category = Category("WELLNESS", Color.GREEN),
                    startTime = rqState.time,
                    duration = rqState.duration ?: Constants.QUEST_MIN_DURATION,
                    reminder = reminder,
                    repeatingPattern = rqState.repeatingPattern!!,
                    note = rqState.note
                )

                val errors = Validator.validate(rqParams).check<ValidationError> {
                    "name" {
                        given { name.isEmpty() } addError ValidationError.EMPTY_NAME
                    }
                }

                if (errors.isNotEmpty()) {
                    dispatch(AddQuestAction.SaveInvalidRepeatingQuest(errors.first()))
                } else {
                    dispatch(AddQuestAction.RepeatingQuestSaved)
                    saveRepeatingQuestUseCase.execute(rqParams)
                }
            }

            is EditRepeatingQuestAction.Save -> {
                val rqState = state.stateFor(EditRepeatingQuestViewState::class.java)

                val rqParams = SaveRepeatingQuestUseCase.Params(
                    id = rqState.id,
                    name = action.name,
                    subQuestNames = action.subQuestNames,
                    color = rqState.color,
                    icon = rqState.icon,
                    category = Category("WELLNESS", Color.GREEN),
                    startTime = rqState.startTime,
                    duration = rqState.duration,
                    reminder = rqState.reminder,
                    repeatingPattern = rqState.repeatingPattern
                )

                val errors = Validator.validate(rqParams).check<ValidationError> {
                    "name" {
                        given { name.isEmpty() } addError ValidationError.EMPTY_NAME
                    }
                }

                if (errors.isNotEmpty()) {
                    dispatch(EditRepeatingQuestAction.SaveInvalidQuest(errors.first()))
                } else {
                    dispatch(EditRepeatingQuestAction.QuestSaved)
                    saveRepeatingQuestUseCase.execute(rqParams)
                }
            }

            is RepeatingQuestAction.Remove ->
                removeRepeatingQuestUseCase.execute(RemoveRepeatingQuestUseCase.Params(action.repeatingQuestId))
        }
    }

    enum class ValidationError {
        EMPTY_NAME
    }


    override fun canHandle(action: Action) =
        action is EditRepeatingQuestAction.Save
            || action is AddQuestAction.SaveRepeatingQuest
            || action is RepeatingQuestAction.Remove
            || action is RepeatingQuestAction.Load
}