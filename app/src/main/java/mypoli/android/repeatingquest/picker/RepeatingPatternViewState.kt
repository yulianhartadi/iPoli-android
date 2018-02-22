package mypoli.android.repeatingquest.picker

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.picker.RepeatingPatternViewState.FrequencyType.DAILY
import mypoli.android.repeatingquest.picker.RepeatingPatternViewState.StateType.*
import org.threeten.bp.DayOfWeek

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/21/18.
 */
sealed class RepeatingPatternAction : Action {
    data class LoadData(val repeatingPattern: RepeatingPattern?) : RepeatingPatternAction()
    data class ChangeFrequency(val position: Int) : RepeatingPatternAction()
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
                val selectedMonthDays =
                    if (pattern != null) selectedMonthDaysFor(pattern) else subState.selectedMonthDays
                subState.copy(
                    type = typeFor(frequencyType),
                    frequencyType = frequencyType,
                    selectedFrequencyIndex = frequencyIndexFor(frequencyType),
                    weekCount = Math.max(selectedWeekDays.size, subState.weekCount),
                    monthCount = Math.max(selectedMonthDays.size, subState.monthCount),
                    selectedWeekDays = selectedWeekDays,
                    selectedMonthDays = selectedMonthDays
                )
            }

            is RepeatingPatternAction.ChangeFrequency -> {
                val frequencyType = createDefaultFrequencyType(action.position)
                subState.copy(
                    type = typeFor(frequencyType),
                    frequencyType = frequencyType,
                    selectedFrequencyIndex = frequencyIndexFor(frequencyType)
                )
            }

            else -> {
                subState
            }
        }
    }

    private fun typeFor(frequencyType: RepeatingPatternViewState.FrequencyType) =
        when (frequencyType) {
            RepeatingPatternViewState.FrequencyType.DAILY -> SHOW_DAILY
            RepeatingPatternViewState.FrequencyType.WEEKLY -> SHOW_WEEKLY
            RepeatingPatternViewState.FrequencyType.MONTHLY -> SHOW_MONTHLY
            RepeatingPatternViewState.FrequencyType.YEARLY -> SHOW_YEARLY
        }

    private fun createDefaultFrequencyType(position: Int) =
        when (position) {
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
            selectedFrequencyIndex = 0,
            weekCount = 1,
            monthCount = 1,
            selectedWeekDays = setOf(),
            selectedMonthDays = setOf()
        )

}

data class RepeatingPatternViewState(
    val type: RepeatingPatternViewState.StateType,
    val frequencyType: FrequencyType,
    val selectedFrequencyIndex: Int,
    val weekCount: Int,
    val monthCount: Int,
    val selectedWeekDays: Set<DayOfWeek>,
    val selectedMonthDays: Set<Int>
    ) : ViewState {
    enum class StateType {
        LOADING,
        SHOW_DAILY,
        SHOW_WEEKLY,
        SHOW_MONTHLY,
        SHOW_YEARLY
    }

    enum class FrequencyType {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }
}