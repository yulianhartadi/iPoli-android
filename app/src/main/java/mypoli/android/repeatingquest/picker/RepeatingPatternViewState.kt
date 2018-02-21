package mypoli.android.repeatingquest.picker

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/21/18.
 */
sealed class RepeatingPatternAction : Action


object RepeatingPatternReducer : BaseViewStateReducer<RepeatingPatternViewState>() {

    override val stateKey = key<RepeatingPatternViewState>()

    override fun reduce(
        state: AppState,
        subState: RepeatingPatternViewState,
        action: Action
    ): RepeatingPatternViewState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun defaultState() =
        RepeatingPatternViewState(RepeatingPatternViewState.StateType.LOADING)

}

data class RepeatingPatternViewState(
    val type: RepeatingPatternViewState.StateType
) : ViewState {
    enum class StateType {
        LOADING
    }
}