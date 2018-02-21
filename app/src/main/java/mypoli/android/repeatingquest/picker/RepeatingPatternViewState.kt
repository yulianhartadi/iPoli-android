package mypoli.android.repeatingquest.picker

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.repeatingquest.entity.RepeatingPattern
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
                subState.copy(
                    type = RepeatingPatternViewState.StateType.DATA_LOADED,
                    repeatingPattern = action.repeatingPattern ?: subState.repeatingPattern
                )
            }

            is RepeatingPatternAction.ChangeFrequency -> {
                subState.copy(
                    type = RepeatingPatternViewState.StateType.FREQUENCY_CHANGED,
                    repeatingPattern = createDefaultRepeatingPattern(action.position)
                )
            }

            else -> {
                subState
            }
        }
    }

    private fun createDefaultRepeatingPattern(position: Int) =
        when (position) {
            1 -> RepeatingPattern.Weekly(setOf())
            2 -> RepeatingPattern.Monthly(setOf())
            3 -> RepeatingPattern.Yearly(LocalDate.now().dayOfMonth, LocalDate.now().monthValue)
            else -> RepeatingPattern.Daily
        }

    override fun defaultState() =
        RepeatingPatternViewState(
            RepeatingPatternViewState.StateType.LOADING,
            RepeatingPattern.Daily
        )

}

data class RepeatingPatternViewState(
    val type: RepeatingPatternViewState.StateType,
    val repeatingPattern: RepeatingPattern
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        FREQUENCY_CHANGED
    }
}

val RepeatingPatternViewState.selectedFrequencyIndex: Int
    get() {
        return when (repeatingPattern) {
            RepeatingPattern.Daily -> 0
            is RepeatingPattern.Weekly -> 1
            is RepeatingPattern.Flexible.Weekly -> 1
            is RepeatingPattern.Monthly -> 2
            is RepeatingPattern.Flexible.Monthly -> 2
            is RepeatingPattern.Yearly -> 3
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