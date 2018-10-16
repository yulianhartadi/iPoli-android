package io.ipoli.android.habit.show

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.datesAhead
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.habit.show.HabitViewState.StateType.DATA_CHANGED
import io.ipoli.android.habit.show.HabitViewState.StateType.LOADING
import io.ipoli.android.habit.show.HabitViewState.WeekProgress
import io.ipoli.android.habit.usecase.CreateHabitHistoryItemsUseCase
import io.ipoli.android.quest.Color
import io.ipoli.android.tag.Tag
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.TemporalAdjusters

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 10/11/18.
 */
sealed class HabitAction : Action {
    data class Load(val habitId: String) : HabitAction()
    data class ToggleHistory(val habitId: String, val date: LocalDate) : HabitAction()
    data class ChangeMonth(val habitId: String, val yearMonth: YearMonth) : HabitAction()
    data class Remove(val habitId: String) : HabitAction() {
        override fun toMap() = mapOf("habitId" to habitId)
    }
}


object HabitReducer : BaseViewStateReducer<HabitViewState>() {
    override val stateKey = key<HabitViewState>()

    override fun reduce(state: AppState, subState: HabitViewState, action: Action) =
        when (action) {

            is DataLoadedAction.HabitChanged -> {
                createWeekProgress(action.habit)
                createChangedState(subState, action.habit).copy(
                    currentDate = action.currentDate,
                    history = action.habitHistory
                )
            }

            is HabitAction.ToggleHistory ->
                if (action.date.isAfter(LocalDate.now()))
                    subState.copy(
                        type = HabitViewState.StateType.TOGGLE_FUTURE_DATE_ERROR
                    )
                else subState

            else -> subState
        }

    private fun createChangedState(
        subState: HabitViewState,
        habit: Habit
    ) =
        subState.copy(
            type = DATA_CHANGED,
            id = habit.id,
            name = habit.name,
            color = habit.color,
            currentStreak = habit.streak.current,
            bestStreak = habit.streak.best,
            successRate = habit.successRate,
            timesADay = habit.timesADay,
            tags = habit.tags,
            weekProgress = createWeekProgress(habit),
            note = habit.note
        )

    private fun createWeekProgress(habit: Habit): List<WeekProgress> {
        val today = LocalDate.now()
        return today
            .with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek))
            .datesAhead(7)
            .mapNotNull {
                when {
                    !habit.shouldBeDoneOn(it) -> null
                    it.isAfter(today) -> WeekProgress.TODO
                    habit.isCompletedForDate(it) -> WeekProgress.COMPLETE
                    else -> WeekProgress.INCOMPLETE
                }
            }
    }

    override fun defaultState() = HabitViewState(
        type = LOADING,
        id = null,
        name = null,
        color = null,
        currentStreak = null,
        bestStreak = null,
        successRate = null,
        timesADay = null,
        tags = emptyList(),
        note = null,
        currentDate = LocalDate.now(),
        weekProgress = emptyList(),
        history = emptyList()
    )
}


data class HabitViewState(
    val type: StateType,
    val id: String?,
    val name: String?,
    val color: Color?,
    val currentStreak: Int?,
    val bestStreak: Int?,
    val successRate: Int?,
    val timesADay: Int?,
    val tags: List<Tag>,
    val note: String?,
    val currentDate: LocalDate,
    val weekProgress: List<WeekProgress>,
    val history: List<CreateHabitHistoryItemsUseCase.HabitHistoryItem>
) : BaseViewState() {

    enum class StateType {
        LOADING,
        DATA_CHANGED,
        TOGGLE_FUTURE_DATE_ERROR
    }

    enum class WeekProgress {
        COMPLETE, INCOMPLETE, TODO
    }
}