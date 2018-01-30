package mypoli.android.quest.agenda

import mypoli.android.common.AppState
import mypoli.android.common.AppStateReducer
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.State
import mypoli.android.quest.agenda.usecase.CreateAgendaItemsUseCase
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/26/2018.
 */

sealed class AgendaAction : Action {
    data class LoadBefore(val visiblePosition: Int) : AgendaAction()
    data class LoadAfter(val visiblePosition: Int) : AgendaAction()
}

data class AgendaState(
    val type: StateType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val agendaItems: List<CreateAgendaItemsUseCase.AgendaItem>,
    val scrollToPosition: Int = -1
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
                    val scrollToPosition = if(state.agendaState.agendaItems.isEmpty()) {
                        ITEMS_BEFORE_COUNT
                    } else -1
                    it.copy(
                        type = AgendaState.StateType.DATA_CHANGED,
                        startDate = action.start,
                        endDate = action.end,
                        agendaItems = action.agendaItems,
                        scrollToPosition = scrollToPosition
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

    override fun defaultState() = AgendaState(
        AgendaState.StateType.LOADING,
        LocalDate.now(),
        LocalDate.now(),
        listOf()
    )

    const val ITEMS_BEFORE_COUNT = 30
    const val ITEMS_AFTER_COUNT = 50
}

data class AgendaViewState(
    val type: AgendaState.StateType,
    val agendaItems: List<AgendaViewController.AgendaViewModel>,
    val scrollToPosition: Int = -1
) : ViewState