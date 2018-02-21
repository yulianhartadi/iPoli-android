package mypoli.android.repeatingquest.picker

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.repeatingquest.entity.RepeatingPattern

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/21/18.
 */
sealed class RepeatingPatternAction : Action {
    data class LoadData(val repeatingPattern: RepeatingPattern?) : RepeatingPatternAction()
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
            else -> {
                subState
            }
        }
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
        DATA_LOADED
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