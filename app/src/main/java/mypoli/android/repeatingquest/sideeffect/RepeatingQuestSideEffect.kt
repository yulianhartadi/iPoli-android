package mypoli.android.repeatingquest.sideeffect

import mypoli.android.common.AppSideEffect
import mypoli.android.common.AppState
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.Validator
import mypoli.android.common.datetime.DateUtils
import mypoli.android.common.redux.Action
import mypoli.android.quest.Category
import mypoli.android.quest.Color
import mypoli.android.repeatingquest.edit.EditRepeatingQuestAction
import mypoli.android.repeatingquest.edit.EditRepeatingQuestViewState
import mypoli.android.repeatingquest.show.RepeatingQuestAction
import mypoli.android.repeatingquest.usecase.CreateRepeatingQuestHistoryUseCase
import mypoli.android.repeatingquest.usecase.RemoveRepeatingQuestUseCase
import mypoli.android.repeatingquest.usecase.SaveRepeatingQuestUseCase
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/03/2018.
 */
class RepeatingQuestSideEffect : AppSideEffect() {
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

            EditRepeatingQuestAction.Save -> {
                val rqState = state.stateFor(EditRepeatingQuestViewState::class.java)

                val rqParams = SaveRepeatingQuestUseCase.Params(
                    id = rqState.id,
                    name = rqState.name,
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

            is RepeatingQuestAction.Remove -> {
                removeRepeatingQuestUseCase.execute(RemoveRepeatingQuestUseCase.Params(action.repeatingQuestId))
            }
        }
    }

    enum class ValidationError {
        EMPTY_NAME
    }


    override fun canHandle(action: Action) =
        action == EditRepeatingQuestAction.Save
            || action is RepeatingQuestAction.Remove
            || action is RepeatingQuestAction.Load
}