package io.ipoli.android.repeatingquest.show

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Minute
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.minutes
import io.ipoli.android.common.mvi.BaseViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.RepeatingQuest
import io.ipoli.android.repeatingquest.entity.PeriodProgress
import io.ipoli.android.repeatingquest.entity.RepeatPattern
import io.ipoli.android.repeatingquest.show.RepeatingQuestViewState.Changed.ProgressModel.COMPLETE
import io.ipoli.android.repeatingquest.show.RepeatingQuestViewState.Changed.ProgressModel.INCOMPLETE
import io.ipoli.android.repeatingquest.show.RepeatingQuestViewState.Changed.RepeatType.*
import io.ipoli.android.repeatingquest.usecase.CreateRepeatingQuestHistoryUseCase
import io.ipoli.android.tag.Tag
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/21/2018.
 */

sealed class RepeatingQuestAction : Action {
    data class Load(val repeatingQuestId: String) : RepeatingQuestAction()
    data class Remove(val repeatingQuestId: String) : RepeatingQuestAction()
}

sealed class RepeatingQuestViewState(open val id: String) : BaseViewState() {

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
        val tags: List<Tag>,
        val subQuestNames: List<String>,
        val color: Color,
        val nextScheduledDate: LocalDate?,
        val totalDuration: Duration<Minute>,
        val currentStreak: Int,
        val repeat: RepeatType,
        val progress: List<ProgressModel>,
        val startTime: Time?,
        val endTime: Time?,
        val duration: Int,
        val isCompleted: Boolean,
        val note: String?
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
            tags = rq.tags,
            subQuestNames = rq.subQuests.map { it.name },
            color = rq.color,
            nextScheduledDate = rq.nextDate,
            totalDuration = 180.minutes,
            currentStreak = 10,
            repeat = repeatTypeFor(rq.repeatPattern),
            progress = progressFor(rq.periodProgress!!),
            startTime = rq.startTime,
            endTime = rq.endTime,
            duration = rq.duration,
            isCompleted = rq.isCompleted,
            note = rq.note
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

    private fun repeatTypeFor(repeatPattern: RepeatPattern) =
        when (repeatPattern) {
            is RepeatPattern.Daily -> Daily
            is RepeatPattern.Weekly, is RepeatPattern.Flexible.Weekly -> Weekly(
                repeatPattern.periodCount
            )

            is RepeatPattern.Monthly, is RepeatPattern.Flexible.Monthly -> Monthly(
                repeatPattern.periodCount
            )

            is RepeatPattern.Yearly ->
                Yearly
        }

    override fun defaultState() = RepeatingQuestViewState.Loading("")
}