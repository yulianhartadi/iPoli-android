package io.ipoli.android.challenge.show

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.datetime.datesBetween

import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.quest.BaseQuest
import io.ipoli.android.quest.Color
import io.ipoli.android.tag.Tag
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/05/2018.
 */

sealed class ChallengeAction : Action {
    data class Load(val challengeId: String) : ChallengeAction() {
        override fun toMap() = mapOf("challengeId" to challengeId)
    }

    data class Remove(val challengeId: String) : ChallengeAction() {
        override fun toMap() = mapOf("challengeId" to challengeId)
    }

    data class RemoveQuestFromChallenge(val questIndex: Int) : ChallengeAction() {
        override fun toMap() = mapOf("questIndex" to questIndex)
    }

    data class RemoveHabitFromChallenge(val habitId: String) : ChallengeAction() {
        override fun toMap() = mapOf("habitId" to habitId)
    }

    data class Complete(val challengeId: String) : ChallengeAction() {
        override fun toMap() = mapOf("challengeId" to challengeId)
    }
}

object ChallengeReducer : BaseViewStateReducer<ChallengeViewState>() {
    override fun reduce(
        state: AppState,
        subState: ChallengeViewState,
        action: Action
    ): ChallengeViewState {
        return when (action) {
            is ChallengeAction.Load -> {
                val dataState = state.dataState
                val c =
                    dataState.challenges!!.firstOrNull { it.id == action.challengeId }

                c?.let {
                    createChangedState(it, subState)
                } ?: subState.copy(type = ChallengeViewState.StateType.LOADING)
            }

            is DataLoadedAction.ChallengesChanged -> {
                val c = action.challenges.firstOrNull { it.id == subState.id }

                c?.let {
                    createChangedState(it, subState)
                } ?: subState.copy(type = ChallengeViewState.StateType.REMOVED)
            }
            else -> subState
        }
    }

    private fun createChangedState(
        challenge: Challenge,
        state: ChallengeViewState
    ): ChallengeViewState {
        val progress = challenge.progress
        val today = LocalDate.now()
        val history = progress.history.withDefault { 0f }
        var progressSum = 0f
        val chartData = today.minusDays(30).datesBetween(today).map {
            progressSum += history.getValue(it)
            it to Math.min(progressSum, 100f)
        }.toMap().toSortedMap()
        return state.copy(
            id = challenge.id,
            type = ChallengeViewState.StateType.DATA_CHANGED,
            name = challenge.name,
            tags = challenge.tags,
            color = challenge.color,
            difficulty = challenge.difficulty.name.toLowerCase().capitalize(),
            endDate = challenge.endDate,
            nextDate = challenge.nextDate,
            completedCount = progress.completedCount,
            totalCount = progress.allCount,
            progressPercent = ((progress.completedCount.toFloat() / progress.allCount) * 100).toInt(),
            xAxisLabelCount = 5,
            chartData = chartData,
            yAxisMax = Math.min(progressSum.toInt() + 10, 100),
            quests = challenge.baseQuests,
            canComplete = !challenge.isCompleted,
            canEdit = !challenge.isCompleted,
            motivations = challenge.motivations,
            note = challenge.note
        )
    }

    override fun defaultState() = ChallengeViewState(
        type = ChallengeViewState.StateType.LOADING,
        id = "",
        name = "",
        tags = listOf(),
        color = Color.PINK,
        difficulty = "",
        endDate = LocalDate.now(),
        nextDate = null,
        completedCount = -1,
        totalCount = -1,
        progressPercent = -1,
        xAxisLabelCount = -1,
        yAxisMax = -1,
        chartData = sortedMapOf(),
        quests = emptyList(),
        habits = emptyList(),
        canEdit = false,
        canComplete = false,
        motivations = emptyList(),
        note = null
    )

    override val stateKey = key<ChallengeViewState>()
}

data class ChallengeViewState(
    val type: StateType,
    val id: String,
    val name: String,
    val tags: List<Tag>,
    val color: Color,
    val difficulty: String,
    val endDate: LocalDate,
    val nextDate: LocalDate?,
    val completedCount: Int,
    val totalCount: Int,
    val progressPercent: Int,
    val xAxisLabelCount: Int,
    val yAxisMax: Int,
    val chartData: SortedMap<LocalDate, Float>,
    val quests: List<BaseQuest>,
    val habits: List<Habit>,
    val canEdit: Boolean,
    val canComplete: Boolean,
    val motivations: List<String>,
    val note: String?
) : BaseViewState() {

    enum class StateType { LOADING, DATA_CHANGED, REMOVED }
}