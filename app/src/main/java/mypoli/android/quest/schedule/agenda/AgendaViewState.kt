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
    val agendaItems: List<CreateAgendaItemsUseCase.AgendaItem>,
    val scrollToPosition: Int?,
    val userScrollPosition: Int?
) : State {
    enum class StateType {
        LOADING,
        DATA_CHANGED,
        SHOW_TOP_LOADER,
        SHOW_BOTTOM_LOADER,
        IDLE
    }
}

object AgendaReducer : AppStateReducer<AgendaState> {
    override fun reduce(state: AppState, action: Action): AgendaState {
        val agendaState = state.agendaState
        return agendaState.let {
            when (action) {
                is DataLoadedAction.AgendaItemsChanged -> {
                    val userScrolledToPosition =
                        if (agendaState.userScrollPosition != null) {
                            val userDate =
                                agendaState.agendaItems[agendaState.userScrollPosition].startDate()
                            findItemPositionToScrollTo(userDate, action.agendaItems)
                        } else null

                    it.copy(
                        type = AgendaState.StateType.DATA_CHANGED,
                        agendaItems = action.agendaItems,
                        scrollToPosition = findItemPositionToScrollTo(
                            action.currentAgendaItemDate,
                            action.agendaItems
                        ),
                        userScrollPosition = userScrolledToPosition
                    )
                }
                is AgendaAction.LoadBefore -> {
                    it.copy(
                        type = AgendaState.StateType.SHOW_TOP_LOADER,
                        userScrollPosition = action.itemPosition
                    )
                }
                is AgendaAction.LoadAfter -> {
                    it.copy(
                        type = AgendaState.StateType.SHOW_BOTTOM_LOADER,
                        userScrollPosition = action.itemPosition
                    )
                }
                is AgendaAction.FirstVisibleItemChanged -> {
                    it.copy(
                        type = AgendaState.StateType.IDLE,
                        userScrollPosition = action.itemPosition
                    )
                }
                else -> it
            }
        }
    }

    private fun findItemPositionToScrollTo(
        date: LocalDate?,
        agendaItems: List<CreateAgendaItemsUseCase.AgendaItem>
    ) = date?.let {
        val currentAgendaItemDate = it
        val index = agendaItems.indexOfLast {
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
        agendaItems = listOf(),
        scrollToPosition = null,
        userScrollPosition = null
    )

    const val ITEMS_BEFORE_COUNT = 25
    const val ITEMS_AFTER_COUNT = 35
}

data class AgendaViewState(
    val type: AgendaState.StateType,
    val agendaItems: List<AgendaViewController.AgendaViewModel>,
    val userScrollPosition: Int?,
    val scrollToPosition: Int?,
    val shouldScrollToUserPosition: Boolean
) : ViewState