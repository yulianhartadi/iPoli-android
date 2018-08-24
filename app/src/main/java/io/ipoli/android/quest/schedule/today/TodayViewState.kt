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
import io.ipoli.android.quest.schedule.today.usecase.CreateTodayItemsUseCase
import org.threeten.bp.LocalDate

sealed class TodayAction : Action {
    data class Load(val today: LocalDate) : TodayAction()

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

            is TodayAction.Load ->
                if (subState.type == TodayViewState.StateType.LOADING) {
                    subState.copy(type = TodayViewState.StateType.SHOW_IMAGE)
                } else subState

            is TodayAction.ImageLoaded ->
                subState.copy(
                    type = if (subState.quests != null) TodayViewState.StateType.SHOW_STATS else TodayViewState.StateType.LOADING,
                    isImageAnimated = true
                )

            is TodayAction.StatsShown -> {
                val type =
                    if (subState.quests != null && subState.todayHabitItems != null)
                        TodayViewState.StateType.SHOW_DATA
                    else
                        TodayViewState.StateType.LOADING
                subState.copy(type = type, areStatsAnimated = true)
            }

            is DataLoadedAction.TodayQuestItemsChanged -> {
                val type = if (!subState.areStatsAnimated || subState.todayHabitItems == null)
                    TodayViewState.StateType.LOADING
                else if (subState.quests == null)
                    TodayViewState.StateType.SHOW_DATA
                else
                    TodayViewState.StateType.QUESTS_CHANGED
                subState.copy(
                    type = type,
                    quests = action.questItems,
                    awesomenessScore = action.awesomenessScore,
                    focusDuration = action.focusDuration,
                    dailyChallengeProgress = action.dailyChallengeProgress
                )
            }

            is DataLoadedAction.HabitItemsChanged -> {

                val type = if (!subState.areStatsAnimated || subState.quests == null)
                    TodayViewState.StateType.LOADING
                else if (subState.todayHabitItems == null)
                    TodayViewState.StateType.SHOW_DATA
                else
                    TodayViewState.StateType.HABITS_CHANGED

                subState.copy(
                    type = type,
                    todayHabitItems = action.habitItems
                        .filterIsInstance(CreateHabitItemsUseCase.HabitItem.Today::class.java)
                )
            }

            else -> subState
        }

    override fun defaultState() =
        TodayViewState(
            type = TodayViewState.StateType.LOADING,
            quests = null,
            todayHabitItems = null,
            awesomenessScore = null,
            focusDuration = null,
            dailyChallengeProgress = null,
            isImageAnimated = false,
            areStatsAnimated = false
        )
}

data class TodayViewState(
    val type: StateType,
    val quests: CreateTodayItemsUseCase.Result?,
    val todayHabitItems: List<CreateHabitItemsUseCase.HabitItem.Today>?,
    val awesomenessScore: Double?,
    val focusDuration: Duration<Minute>?,
    val dailyChallengeProgress: CheckDailyChallengeProgressUseCase.Result?,
    val isImageAnimated: Boolean,
    val areStatsAnimated: Boolean
) :
    BaseViewState() {

    enum class StateType {
        LOADING,
        HABITS_CHANGED,
        QUESTS_CHANGED,
        SHOW_IMAGE,
        SHOW_STATS,
        SHOW_DATA
    }
}