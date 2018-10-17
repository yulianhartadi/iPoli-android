package io.ipoli.android.challenge.show

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.quest.BaseQuest
import io.ipoli.android.quest.Color
import io.ipoli.android.tag.Tag
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
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

    class LogValue(
        val challengeId: String,
        val trackValueId: String,
        val log: Challenge.TrackedValue.Log
    ) :
        ChallengeAction()

    data class ChangeProgressMonth(val challengeId: String, val yearMonth: YearMonth) :
        ChallengeAction()
}

object ChallengeReducer : BaseViewStateReducer<ChallengeViewState>() {
    override fun reduce(
        state: AppState,
        subState: ChallengeViewState,
        action: Action
    ) =
        when (action) {
            is DataLoadedAction.ChallengeChanged -> {
                val challenge = action.challenge
                createChangedState(challenge, subState).copy(
                    currentDate = action.currentDate,
                    progressItems = if (challenge.hasProgress) {
                        val progress =
                            challenge.trackedValues.first { it is Challenge.TrackedValue.Progress }
                        (progress as Challenge.TrackedValue.Progress).items
                    } else subState.progressItems
                )
            }

            else -> subState
        }

    private fun createChangedState(
        challenge: Challenge,
        state: ChallengeViewState
    ) =
        state.copy(
            id = challenge.id,
            type = ChallengeViewState.StateType.DATA_CHANGED,
            name = challenge.name,
            tags = challenge.tags,
            color = challenge.color,
            difficulty = challenge.difficulty.name.toLowerCase().capitalize(),
            endDate = challenge.endDate,
            nextDate = challenge.nextDate,
            trackedValues = challenge.trackedValues,
            xAxisLabelCount = 5,
            quests = challenge.baseQuests,
            habits = challenge.habits,
            canComplete = !challenge.isCompleted && !challenge.isFromPreset,
            canEdit = !challenge.isCompleted && !challenge.isFromPreset,
            canAdd = !challenge.isCompleted && !challenge.isFromPreset,
            motivations = challenge.motivations,
            note = challenge.note
        )

    override fun defaultState() =
        ChallengeViewState(
            type = ChallengeViewState.StateType.LOADING,
            id = "",
            name = "",
            tags = listOf(),
            color = Color.PINK,
            difficulty = "",
            endDate = LocalDate.now(),
            nextDate = null,
            trackedValues = emptyList(),
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
            canAdd = false,
            motivations = emptyList(),
            note = null,
            currentDate = LocalDate.now(),
            progressItems = null
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
    val trackedValues: List<Challenge.TrackedValue>,
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
    val canAdd: Boolean,
    val motivations: List<String>,
    val note: String?,
    val currentDate: LocalDate,
    val progressItems: List<Challenge.TrackedValue.Progress.DayProgress>?
) : BaseViewState() {

    enum class StateType { LOADING, DATA_CHANGED, REMOVED }
}