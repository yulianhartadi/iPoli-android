package io.ipoli.android.quest.schedule.summary

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.quest.schedule.summary.usecase.CreateScheduleSummaryUseCase
import io.ipoli.android.quest.usecase.Schedule
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 07/03/2018.
 */
sealed class ScheduleSummaryAction : Action {

    data class Load(val currentDate: LocalDate) : ScheduleSummaryAction()
    data class ChangeDate(val currentDate: LocalDate) : ScheduleSummaryAction()
    data class RescheduleQuest(val questId: String, val date: LocalDate?) : ScheduleSummaryAction()
    data class RemoveQuest(val questId: String) : ScheduleSummaryAction()
    data class UndoRemoveQuest(val questId: String) : ScheduleSummaryAction()
}

object ScheduleSummaryReducer : BaseViewStateReducer<ScheduleSummaryViewState>() {

    override val stateKey = key<ScheduleSummaryViewState>()

    override fun reduce(
        state: AppState,
        subState: ScheduleSummaryViewState,
        action: Action
    ) =
        when (action) {

            is ScheduleSummaryAction.Load ->
                subState.copy(
                    type = ScheduleSummaryViewState.StateType.DATE_DATA_CHANGED,
                    currentDate = action.currentDate
                )

            is ScheduleSummaryAction.ChangeDate ->
                if (subState.currentDate != action.currentDate) {
                    subState.copy(
                        type = ScheduleSummaryViewState.StateType.DATE_DATA_CHANGED,
                        currentDate = action.currentDate,
                        previousDate = subState.currentDate
                    )
                } else subState

            is DataLoadedAction.ScheduleSummaryChanged -> {
                if (subState.currentDate == action.currentDate) {
                    subState.copy(
                        type = ScheduleSummaryViewState.StateType.SCHEDULE_SUMMARY_DATA_CHANGED,
                        items = action.scheduleSummaryItems
                    )
                } else subState
            }

            is DataLoadedAction.MonthPreviewScheduleChanged -> {
                require(action.schedule.keys.size == 1)
                val schedule = action.schedule[action.schedule.keys.first()]!!
                if (subState.currentDate == schedule.date) {
                    subState.copy(
                        type = ScheduleSummaryViewState.StateType.SCHEDULE_DATA_CHANGED,
                        schedule = schedule
                    )
                } else subState
            }

            else -> subState
        }

    override fun defaultState() =
        ScheduleSummaryViewState(
            type = ScheduleSummaryViewState.StateType.LOADING,
            currentDate = LocalDate.now(),
            previousDate = LocalDate.now(),
            items = emptyList(),
            schedule = null
        )
}

data class ScheduleSummaryViewState(
    val type: StateType,
    val currentDate: LocalDate,
    val previousDate: LocalDate,
    val items: List<CreateScheduleSummaryUseCase.ScheduleSummaryItem>,
    val schedule: Schedule?
) : BaseViewState() {
    enum class StateType {
        LOADING,
        DATE_DATA_CHANGED,
        SCHEDULE_SUMMARY_DATA_CHANGED,
        SCHEDULE_DATA_CHANGED
    }
}