package mypoli.android.repeatingquest.picker

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.picker.RepeatingPatternViewState.FrequencyType.*
import mypoli.android.repeatingquest.picker.RepeatingPatternViewState.StateType.*
import org.threeten.bp.DayOfWeek

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/21/18.
 */
sealed class RepeatingPatternAction : Action {
    data class LoadData(val repeatingPattern: RepeatingPattern?) : RepeatingPatternAction()
    data class ChangeFrequency(val index: Int) : RepeatingPatternAction()
    data class ToggleWeekDay(val weekDay: DayOfWeek) : RepeatingPatternAction()
    data class ChangeWeekDayCount(val index: Int) : RepeatingPatternAction()
}


object RepeatingPatternReducer : BaseViewStateReducer<RepeatingPatternViewState>() {

    override val stateKey = key<RepeatingPatternViewState>()

    override fun reduce(
        state: AppState,
        subState: RepeatingPatternViewState,
        action: Action
    ): RepeatingPatternViewState {
        return when (action) {
            is RepeatingPatternAction.LoadData -> {
                val pattern = action.repeatingPattern
                val frequencyType =
                    if (pattern != null) frequencyTypeFor(pattern)
                    else subState.frequencyType

                val selectedWeekDays =
                    if (pattern != null) selectedWeekDaysFor(pattern) else subState.selectedWeekDays
                val weekDaysCount = Math.max(1, selectedWeekDays.size)
                val weekDaysCountIndex = subState.weekCountValues.indexOfFirst { weekDaysCount == it }

                val selectedMonthDays =
                    if (pattern != null) selectedMonthDaysFor(pattern) else subState.selectedMonthDays
                val monthDaysCount = Math.max(1, selectedMonthDays.size)
                val monthDaysCountIndex = subState.monthCountValues.indexOfFirst { monthDaysCount == it }

                subState.copy(
                    type = typeFor(frequencyType),
                    frequencyType = frequencyType,
                    frequencyIndex = frequencyIndexFor(frequencyType),
                    weekDaysCountIndex = weekDaysCountIndex,
                    monthDaysCountIndex = monthDaysCountIndex,
                    selectedWeekDays = selectedWeekDays,
                    selectedMonthDays = selectedMonthDays,
                    isFlexible = pattern is RepeatingPattern.Flexible
                )
            }

            is RepeatingPatternAction.ChangeFrequency -> {
                val frequencyType = frequencyTypeForIndex(action.index)
                subState.copy(
                    type = typeFor(frequencyType),
                    frequencyType = frequencyType,
                    frequencyIndex = frequencyIndexFor(frequencyType),
                    isFlexible = isFlexible(frequencyType, subState)
                )
            }

            is RepeatingPatternAction.ToggleWeekDay -> {
                val weekDay = action.weekDay
                val selectedWeekDays = if (subState.selectedWeekDays.contains(weekDay)) {
                    subState.selectedWeekDays.minus(weekDay)
                } else {
                    subState.selectedWeekDays.plus(weekDay)
                }

                subState.copy(
                    type = WEEK_DAYS_CHANGED,
                    selectedWeekDays = selectedWeekDays,
                    isFlexible = subState.weekCountValues[subState.weekDaysCountIndex] != selectedWeekDays.size
                )
            }

            is RepeatingPatternAction.ChangeWeekDayCount -> {
                subState.copy(
                    type = COUNT_CHANGED,
                    weekDaysCountIndex = action.index,
                    isFlexible = subState.weekCountValues[action.index] != subState.selectedWeekDays.size
                )
            }

            else -> {
                subState
            }
        }
    }

    private fun isFlexible(
        frequencyType: RepeatingPatternViewState.FrequencyType,
        state: RepeatingPatternViewState
    ) =
        when (frequencyType) {
            DAILY -> false
            YEARLY -> false
            WEEKLY -> {
                val daysCount = state.weekCountValues[state.weekDaysCountIndex]
                daysCount != state.selectedWeekDays.size
            }
            MONTHLY -> {
                val daysCount = state.monthCountValues[state.monthDaysCountIndex]
                daysCount != state.selectedMonthDays.size
            }
        }

    private fun typeFor(frequencyType: RepeatingPatternViewState.FrequencyType) =
        when (frequencyType) {
            RepeatingPatternViewState.FrequencyType.DAILY -> SHOW_DAILY
            RepeatingPatternViewState.FrequencyType.WEEKLY -> SHOW_WEEKLY
            RepeatingPatternViewState.FrequencyType.MONTHLY -> SHOW_MONTHLY
            RepeatingPatternViewState.FrequencyType.YEARLY -> SHOW_YEARLY
        }

    private fun frequencyTypeForIndex(index: Int) =
        when (index) {
            1 -> RepeatingPatternViewState.FrequencyType.WEEKLY
            2 -> RepeatingPatternViewState.FrequencyType.MONTHLY
            3 -> RepeatingPatternViewState.FrequencyType.YEARLY
            else -> RepeatingPatternViewState.FrequencyType.DAILY
        }

    private fun frequencyIndexFor(frequencyType: RepeatingPatternViewState.FrequencyType): Int =
        when (frequencyType) {
            RepeatingPatternViewState.FrequencyType.DAILY -> 0
            RepeatingPatternViewState.FrequencyType.WEEKLY -> 1
            RepeatingPatternViewState.FrequencyType.MONTHLY -> 2
            RepeatingPatternViewState.FrequencyType.YEARLY -> 3
        }

    private fun frequencyTypeFor(pattern: RepeatingPattern) =
        when (pattern) {
            RepeatingPattern.Daily -> RepeatingPatternViewState.FrequencyType.DAILY
            is RepeatingPattern.Weekly -> RepeatingPatternViewState.FrequencyType.WEEKLY
            is RepeatingPattern.Flexible.Weekly -> RepeatingPatternViewState.FrequencyType.WEEKLY
            is RepeatingPattern.Monthly -> RepeatingPatternViewState.FrequencyType.MONTHLY
            is RepeatingPattern.Flexible.Monthly -> RepeatingPatternViewState.FrequencyType.MONTHLY
            is RepeatingPattern.Yearly -> RepeatingPatternViewState.FrequencyType.YEARLY
        }


    private fun selectedWeekDaysFor(pattern: RepeatingPattern): Set<DayOfWeek> =
        when (pattern) {
            is RepeatingPattern.Weekly -> pattern.daysOfWeek
            is RepeatingPattern.Flexible.Weekly -> pattern.preferredDays
            else -> setOf()
        }

    private fun selectedMonthDaysFor(pattern: RepeatingPattern): Set<Int> =
        when (pattern) {
            is RepeatingPattern.Monthly -> pattern.daysOfMonth
            is RepeatingPattern.Flexible.Monthly -> pattern.preferredDays
            else -> setOf()
        }

    override fun defaultState() =
        RepeatingPatternViewState(
            LOADING,
            DAILY,
            frequencyIndex = 0,
            weekDaysCountIndex = 0,
            monthDaysCountIndex = 0,
            selectedWeekDays = setOf(),
            selectedMonthDays = setOf(),
            weekCountValues = (1..6).toList(),
            monthCountValues = (1..31).toList(),
            isFlexible = false
        )

}

data class RepeatingPatternViewState(
    val type: RepeatingPatternViewState.StateType,
    val frequencyType: FrequencyType,
    val frequencyIndex: Int,
    val weekDaysCountIndex: Int,
    val monthDaysCountIndex: Int,
    val selectedWeekDays: Set<DayOfWeek>,
    val selectedMonthDays: Set<Int>,
    val weekCountValues: List<Int>,
    val monthCountValues: List<Int>,
    val isFlexible: Boolean
    ) : ViewState {
    enum class StateType {
        LOADING,
        SHOW_DAILY,
        SHOW_WEEKLY,
        SHOW_MONTHLY,
        SHOW_YEARLY,
        WEEK_DAYS_CHANGED,
        COUNT_CHANGED
    }

    enum class FrequencyType {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }
}