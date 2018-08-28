package io.ipoli.android.habit.list

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction

import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.habit.usecase.CreateHabitItemsUseCase

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/13/18.
 */
sealed class HabitListAction : Action {
    data class CompleteHabit(val habitId: String) : HabitListAction() {
        override fun toMap() = mapOf("habitId" to habitId)
    }

    data class UndoCompleteHabit(val habitId: String) : HabitListAction() {
        override fun toMap() = mapOf("habitId" to habitId)
    }

    object Load : HabitListAction()

    object Add : HabitListAction()

    object AddPreset : HabitListAction()
}

object HabitListReducer : BaseViewStateReducer<HabitListViewState>() {

    override val stateKey = key<HabitListViewState>()

    override fun reduce(
        state: AppState,
        subState: HabitListViewState,
        action: Action
    ) =
        when (action) {

            is DataLoadedAction.HabitItemsChanged ->
                subState.copy(
                    type = HabitListViewState.StateType.DATA_CHANGED,
                    habitItems = action.habitItems,
                    showEmptyView = action.habitItems.isEmpty()
                )

            else -> subState
        }

    override fun defaultState() = HabitListViewState(
        type = HabitListViewState.StateType.LOADING,
        habitItems = null,
        showEmptyView = false
    )
}

data class HabitListViewState(
    val type: StateType,
    val habitItems: List<CreateHabitItemsUseCase.HabitItem>?,
    val showEmptyView: Boolean
) : BaseViewState() {
    enum class StateType {
        LOADING,
        DATA_CHANGED,
        SHOW_ADD,
        SHOW_ADD_PRESET
    }
}