package mypoli.android.quest.schedule.agenda

import mypoli.android.common.AppState
import mypoli.android.common.AppStateReducer
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.datetime.isBetween
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.State
import mypoli.android.quest.schedule.agenda.usecase.CreateAgendaItemsUseCase
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/26/2018.
 */

sealed class AgendaAction : Action {
    data class LoadBefore(val itemPosition: Int) : AgendaAction()
    data class LoadAfter(val itemPosition: Int) : AgendaAction()
    data class CompleteQuest(val itemPosition: Int) : AgendaAction()
    data class UndoCompleteQuest(val itemPosition: Int) : AgendaAction()
    data class FirstVisibleItemChanged(val itemPosition: Int) : AgendaAction()
}

data class AgendaState(
    val type: StateType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val agendaItems: List<CreateAgendaItemsUseCase.AgendaItem>,
    val scrollToPosition: Int?
) : State {
    enum class StateType {
        LOADING,
        DATA_CHANGED,
        SHOW_TOP_LOADER,
        SHOW_BOTTOM_LOADER
    }
}

object AgendaReducer : AppStateReducer<AgendaState> {
    override fun reduce(state: AppState, action: Action) =
        state.agendaState.let {
            when (action) {
                is DataLoadedAction.AgendaItemsChanged -> {

                    it.copy(
                        type = AgendaState.StateType.DATA_CHANGED,
                        startDate = action.start,
                        endDate = action.end,
                        agendaItems = action.agendaItems,
                        scrollToPosition = findItemPositionToScrollTo(action)
                    )
                }
                is AgendaAction.LoadBefore -> {
                    it.copy(
                        type = AgendaState.StateType.SHOW_TOP_LOADER
                    )
                }
                is AgendaAction.LoadAfter -> {
                    it.copy(
                        type = AgendaState.StateType.SHOW_BOTTOM_LOADER
                    )
                }
                else -> it
            }
        }

    private fun findItemPositionToScrollTo(
        action: DataLoadedAction.AgendaItemsChanged
    ) = action.currentAgendaItemDate?.let {
        val currentAgendaItemDate = it
        val index = action.agendaItems.indexOfLast {
            when (it) {
                is CreateAgendaItemsUseCase.AgendaItem.Date ->
                    it.startDate() == currentAgendaItemDate
                is CreateAgendaItemsUseCase.AgendaItem.Week ->
                    currentAgendaItemDate.isBetween(
                        it.start,
                        it.end
                    )
                else -> false
            }
        }
        if (index < 0) null
        else index
    }

    override fun defaultState() = AgendaState(
        type = AgendaState.StateType.LOADING,
        startDate = LocalDate.now(),
        endDate = LocalDate.now(),
        agendaItems = listOf(),
        scrollToPosition = null
    )

    const val ITEMS_BEFORE_COUNT = 30
    const val ITEMS_AFTER_COUNT = 50
}

data class AgendaViewState(
    val type: AgendaState.StateType,
    val agendaItems: List<AgendaViewController.AgendaViewModel>,
    val scrollToPosition: Int?
) : ViewState