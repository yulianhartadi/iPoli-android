package io.ipoli.android.quest.schedule.today

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Minute
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.dailychallenge.usecase.CheckDailyChallengeProgressUseCase
import io.ipoli.android.habit.usecase.CreateHabitItemsUseCase
import io.ipoli.android.quest.schedule.today.TodayViewState.StateType.*
import io.ipoli.android.quest.schedule.today.usecase.CreateTodayItemsUseCase
import org.threeten.bp.LocalDate

sealed class TodayAction : Action {
    data class Load(val today: LocalDate, val showDataAfterStats: Boolean) : TodayAction()

    object ImageLoaded : TodayAction()
    object StatsShown : TodayAction()

    data class CompleteHabit(val habitId: String) : TodayAction() {
        override fun toMap() = mapOf("habitId" to habitId)
    }

    data class UndoCompleteHabit(val habitId: String) : TodayAction() {
        override fun toMap() = mapOf("habitId" to habitId)
    }

    data class RescheduleQuest(val questId: String, val date: LocalDate?) : TodayAction() {
        override fun toMap() = mapOf("questId" to questId, "date" to date)
    }

    data class CompleteQuest(val questId: String) : TodayAction() {
        override fun toMap() = mapOf("questId" to questId)
    }

    data class UndoCompleteQuest(val questId: String) : TodayAction() {
        override fun toMap() = mapOf("questId" to questId)
    }

    data class RemoveQuest(val questId: String) : TodayAction() {
        override fun toMap() = mapOf("questId" to questId)
    }

    data class UndoRemoveQuest(val questId: String) : TodayAction() {
        override fun toMap() = mapOf("questId" to questId)
    }
}

object TodayReducer : BaseViewStateReducer<TodayViewState>() {

    override val stateKey = key<TodayViewState>()

    override fun reduce(state: AppState, subState: TodayViewState, action: Action) =
        when (action) {

            is TodayAction.Load -> {
                val dataState = state.dataState

                val type = if (dataState.todayImage != null) SHOW_IMAGE else LOADING

                subState.copy(
                    type = type,
                    showDataAfterStats = action.showDataAfterStats,
                    todayImageUrl = dataState.todayImage,
                    awesomenessScore = dataState.awesomenessScore,
                    focusDuration = dataState.focusDuration,
                    dailyChallengeProgress = dataState.dailyChallengeProgress
                )
            }

            is DataLoadedAction.TodayImageChanged -> {
                subState.copy(
                    type = SHOW_IMAGE,
                    todayImageUrl = action.imageUrl
                )
            }

            is DataLoadedAction.TodayQuestItemsChanged -> {
                val type =
                    when {
                        subState.todayHabitItems == null -> LOADING
                        subState.quests == null -> DATA_CHANGED
                        else -> QUESTS_CHANGED
                    }
                subState.copy(
                    type = type,
                    quests = action.questItems
                )
            }

            is DataLoadedAction.HabitItemsChanged -> {

                val type =
                    when {
                        subState.quests == null -> LOADING
                        subState.todayHabitItems == null -> DATA_CHANGED
                        else -> HABITS_CHANGED
                    }

                subState.copy(
                    type = type,
                    todayHabitItems = action.habitItems
                        .filterIsInstance(CreateHabitItemsUseCase.HabitItem.Today::class.java)
                )
            }

            is TodayAction.ImageLoaded ->
                if (subState.awesomenessScore == null)
                    subState
                else
                    subState.copy(
                        type = SHOW_SUMMARY_STATS
                    )

            is TodayAction.StatsShown ->
                if (subState.showDataAfterStats)
                    subState.copy(
                    type = SHOW_DATA
                ) else subState

            is DataLoadedAction.TodaySummaryStatsChanged ->
                subState.copy(
                    type = SUMMARY_STATS_CHANGED,
                    awesomenessScore = action.awesomenessScore,
                    focusDuration = action.focusDuration,
                    dailyChallengeProgress = action.dailyChallengeProgress
                )

            else -> subState
        }

    override fun defaultState() =
        TodayViewState(
            type = LOADING,
            quests = null,
            todayHabitItems = null,
            todayImageUrl = null,
            awesomenessScore = null,
            focusDuration = null,
            dailyChallengeProgress = null,
            showDataAfterStats = false
        )
}

data class TodayViewState(
    val type: StateType,
    val quests: CreateTodayItemsUseCase.Result?,
    val todayHabitItems: List<CreateHabitItemsUseCase.HabitItem.Today>?,
    val todayImageUrl: String?,
    val awesomenessScore: Double?,
    val focusDuration: Duration<Minute>?,
    val dailyChallengeProgress: CheckDailyChallengeProgressUseCase.Result?,
    val showDataAfterStats: Boolean
) :
    BaseViewState() {

    enum class StateType {
        LOADING,
        SUMMARY_STATS_CHANGED,
        SHOW_SUMMARY_STATS,
        SHOW_DATA,
        HABITS_CHANGED,
        QUESTS_CHANGED,
        SHOW_IMAGE,
        DATA_CHANGED
    }
}