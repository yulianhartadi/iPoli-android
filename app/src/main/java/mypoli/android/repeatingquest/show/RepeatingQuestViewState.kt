package mypoli.android.repeatingquest.show

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.datetime.Duration
import mypoli.android.common.datetime.Minute
import mypoli.android.common.datetime.minutes
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.quest.Category
import mypoli.android.quest.Color
import mypoli.android.repeatingquest.entity.PeriodProgress
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.entity.RepeatingQuest
import mypoli.android.repeatingquest.show.RepeatingQuestViewState.Changed.ProgressModel.COMPLETE
import mypoli.android.repeatingquest.show.RepeatingQuestViewState.Changed.ProgressModel.INCOMPLETE
import mypoli.android.repeatingquest.show.RepeatingQuestViewState.Changed.RepeatType.*
import mypoli.android.repeatingquest.usecase.RepeatingQuestHistoryUseCase
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
        val history: Map<LocalDate, RepeatingQuestHistoryUseCase.QuestState>
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
        val progress: List<ProgressModel>
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
            progress = progressFor(rq.periodProgress!!)
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