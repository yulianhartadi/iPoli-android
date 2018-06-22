package io.ipoli.android.habit.predefined

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.BaseViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.habit.predefined.PredefinedHabitListViewState.StateType.DATA_LOADED

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/21/18.
 */
sealed class PredefinedHabitListAction : Action {
    object Load : PredefinedHabitListAction()
}

object PredefinedHabitListReducer : BaseViewStateReducer<PredefinedHabitListViewState>() {
    override val stateKey = key<PredefinedHabitListViewState>()

    override fun reduce(
        state: AppState,
        subState: PredefinedHabitListViewState,
        action: Action
    ) =
        when (action) {
            else -> subState
    }

    override fun defaultState() = PredefinedHabitListViewState(
        type = DATA_LOADED
    )


}

data class PredefinedHabitListViewState(
    val type: StateType
) : BaseViewState() {

    enum class StateType {
        DATA_LOADED
    }
}