package io.ipoli.android.repeatingquest.show

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Minute
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.minutes
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.Category
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.RepeatingQuest
import io.ipoli.android.repeatingquest.entity.PeriodProgress
import io.ipoli.android.repeatingquest.entity.RepeatingPattern
import io.ipoli.android.repeatingquest.show.RepeatingQuestViewState.Changed.ProgressModel.COMPLETE
import io.ipoli.android.repeatingquest.show.RepeatingQuestViewState.Changed.ProgressModel.INCOMPLETE
import io.ipoli.android.repeatingquest.show.RepeatingQuestViewState.Changed.RepeatType.*
import io.ipoli.android.repeatingquest.usecase.CreateRepeatingQuestHistoryUseCase
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/21/2018.
 */

sealed class RepeatingQuestAction : Action {
    data class Load(val repeatingQuestId: String) : RepeatingQuestAction()
    data class Remove(val repeatingQuestId: String) : RepeatingQuestAction()
}

sealed class RepeatingQuestViewState(open val id: String) : ViewState {

    data class Loading(override val id: String) :
        RepeatingQuestViewState(id)

    object Removed : RepeatingQuestViewState("")

    data class HistoryChanged(
        override val id: String,
        val history: CreateRepeatingQuestHistoryUseCase.History
    ) : RepeatingQuestViewState(id)

    data class Changed(
        override val id: String,
        val name: String,
        val color: Color,
        val category: Category,
        val nextScheduledDate: LocalDate?,
        val totalDuration: Duration<Minute>,
        val currentStreak: Int,
        val repeat: RepeatType,
        val progress: List<ProgressModel>,
        val startTime: Time?,
        val endTime: Time?,
        val duration: Int,
        val isCompleted: Boolean
    ) : RepeatingQuestViewState(id) {

        enum class ProgressModel {
            COMPLETE, INCOMPLETE
        }

        sealed class RepeatType {
            object Daily : RepeatType()
            data class Weekly(val frequency: Int) : RepeatType()
            data class Monthly(val frequency: Int) : RepeatType()
            object Yearly : RepeatType()
        }
    }
}

object RepeatingQuestReducer : BaseViewStateReducer<RepeatingQuestViewState>() {

    override val stateKey = key<RepeatingQuestViewState>()

    override fun reduce(state: AppState, subState: RepeatingQuestViewState, action: Action) =
        when (action) {
            is RepeatingQuestAction.Load -> {

                val dataState = state.dataState
                val rq =
                    dataState.repeatingQuests.firstOrNull { it.id == action.repeatingQuestId }

                rq?.let {
                    createChangedState(it)
                } ?: RepeatingQuestViewState.Loading(action.repeatingQuestId)
            }

            is DataLoadedAction.RepeatingQuestsChanged -> {

                val rq = action.repeatingQuests.firstOrNull { it.id == subState.id }
                rq?.let {
                    createChangedState(it)
                } ?: RepeatingQuestViewState.Removed
            }

            is DataLoadedAction.RepeatingQuestHistoryChanged -> {
                RepeatingQuestViewState.HistoryChanged(
                    id = subState.id,
                    history = action.history
                )
            }

            is RepeatingQuestAction.Remove -> {
                RepeatingQuestViewState.Removed
            }

            else -> subState
        }

    private fun createChangedState(rq: RepeatingQuest): RepeatingQuestViewState.Changed {
        return RepeatingQuestViewState.Changed(
            id = rq.id,
            name = rq.name,
            color = rq.color,
            category = Category("Chores", Color.BROWN),
            nextScheduledDate = rq.nextDate,
            totalDuration = 180.minutes,
            currentStreak = 10,
            repeat = repeatTypeFor(rq.repeatingPattern),
            progress = progressFor(rq.periodProgress!!),
            startTime = rq.startTime,
            endTime = rq.endTime,
            duration = rq.duration,
            isCompleted = rq.isCompleted
        )
    }

    private fun progressFor(progress: PeriodProgress): List<RepeatingQuestViewState.Changed.ProgressModel> {
        val complete = (0 until progress.completedCount).map {
            COMPLETE
        }
        val incomplete = (progress.completedCount until progress.allCount).map {
            INCOMPLETE
        }
        return complete + incomplete
    }

    private fun repeatTypeFor(repeatingPattern: RepeatingPattern) =
        when (repeatingPattern) {
            is RepeatingPattern.Daily -> Daily
            is RepeatingPattern.Weekly, is RepeatingPattern.Flexible.Weekly -> Weekly(
                repeatingPattern.periodCount
            )

            is RepeatingPattern.Monthly, is RepeatingPattern.Flexible.Monthly -> Monthly(
                repeatingPattern.periodCount
            )

            is RepeatingPattern.Yearly ->
                Yearly
        }

    override fun defaultState() = RepeatingQuestViewState.Loading("")
}