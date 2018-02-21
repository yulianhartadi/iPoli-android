package mypoli.android.repeatingquest.picker

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.picker.RepeatingPatternViewState.StateType.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

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
                val pattern = action.repeatingPattern ?: subState.repeatingPattern
                subState.copy(
                    type = typeFor(pattern),
                    repeatingPattern = pattern,
                    selectedFrequencyIndex = frequencyIndexFor(pattern)
                )
            }

            is RepeatingPatternAction.ChangeFrequency -> {
                val pattern = createDefaultRepeatingPattern(action.position)
                subState.copy(
                    type = typeFor(pattern),
                    repeatingPattern = pattern,
                    selectedFrequencyIndex = frequencyIndexFor(pattern)
                )
            }

            else -> {
                subState
            }
        }
    }

    private fun typeFor(pattern: RepeatingPattern) =
        when (pattern) {
            RepeatingPattern.Daily -> SHOW_DAILY
            is RepeatingPattern.Weekly -> SHOW_WEEKLY
            is RepeatingPattern.Flexible.Weekly -> SHOW_WEEKLY
            is RepeatingPattern.Monthly -> SHOW_MONTHLY
            is RepeatingPattern.Flexible.Monthly -> SHOW_MONTHLY
            is RepeatingPattern.Yearly -> SHOW_YEARLY
        }

    private fun createDefaultRepeatingPattern(position: Int) =
        when (position) {
            1 -> RepeatingPattern.Weekly(setOf())
            2 -> RepeatingPattern.Monthly(setOf())
            3 -> RepeatingPattern.Yearly(LocalDate.now().dayOfMonth, LocalDate.now().monthValue)
            else -> RepeatingPattern.Daily
        }

    private fun frequencyIndexFor(pattern: RepeatingPattern): Int =
        when (pattern) {
            RepeatingPattern.Daily -> 0
            is RepeatingPattern.Weekly -> 1
            is RepeatingPattern.Flexible.Weekly -> 1
            is RepeatingPattern.Monthly -> 2
            is RepeatingPattern.Flexible.Monthly -> 2
            is RepeatingPattern.Yearly -> 3
        }

    override fun defaultState() =
        RepeatingPatternViewState(
            LOADING,
            RepeatingPattern.Daily,
            selectedFrequencyIndex = 0
        )

}

data class RepeatingPatternViewState(
    val type: RepeatingPatternViewState.StateType,
    val repeatingPattern: RepeatingPattern,
    val selectedFrequencyIndex: Int
    ) : ViewState {
    enum class StateType {
        LOADING,
        SHOW_DAILY,
        SHOW_WEEKLY,
        SHOW_MONTHLY,
        SHOW_YEARLY
    }
}

val RepeatingPatternViewState.count: Int
    get() {
        return when (repeatingPattern) {
            RepeatingPattern.Daily -> 1
            is RepeatingPattern.Weekly -> repeatingPattern.daysOfWeek.size
            is RepeatingPattern.Flexible.Weekly -> repeatingPattern.timesPerWeek
            is RepeatingPattern.Monthly -> repeatingPattern.daysOfMonth.size
            is RepeatingPattern.Flexible.Monthly -> repeatingPattern.timesPerMonth
            is RepeatingPattern.Yearly -> 1
        }
    }

val RepeatingPatternViewState.showWeekDays: Boolean
    get() {
        return when (repeatingPattern) {
            RepeatingPattern.Daily -> false
            is RepeatingPattern.Weekly -> true
            is RepeatingPattern.Flexible.Weekly -> true
            is RepeatingPattern.Monthly -> false
            is RepeatingPattern.Flexible.Monthly -> false
            is RepeatingPattern.Yearly -> false
        }
    }

val RepeatingPatternViewState.showMonthDays: Boolean
    get() {
        return when (repeatingPattern) {
            RepeatingPattern.Daily -> false
            is RepeatingPattern.Weekly -> false
            is RepeatingPattern.Flexible.Weekly -> false
            is RepeatingPattern.Monthly -> true
            is RepeatingPattern.Flexible.Monthly -> true
            is RepeatingPattern.Yearly -> false
        }
    }

val RepeatingPatternViewState.showYearDay: Boolean
    get() {
        return when (repeatingPattern) {
            RepeatingPattern.Daily -> false
            is RepeatingPattern.Weekly -> false
            is RepeatingPattern.Flexible.Weekly -> false
            is RepeatingPattern.Monthly -> false
            is RepeatingPattern.Flexible.Monthly -> false
            is RepeatingPattern.Yearly -> true
        }
    }

val RepeatingPatternViewState.selectedWeekDays: Set<DayOfWeek>
    get() {
        return when (repeatingPattern) {
            is RepeatingPattern.Weekly -> repeatingPattern.daysOfWeek
            is RepeatingPattern.Flexible.Weekly -> repeatingPattern.preferredDays
            else -> setOf()
        }
    }