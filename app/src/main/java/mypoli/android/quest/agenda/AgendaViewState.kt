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
    data class LoadBefore(val date: LocalDate) : AgendaAction()
    data class LoadAfter(val date: LocalDate) : AgendaAction()
}

data class AgendaState(
    val type: StateType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val agendaItems: List<CreateAgendaItemsUseCase.AgendaItem>
) : State {
    enum class StateType {
        LOADING,
        DATA_CHANGED
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
                        agendaItems = action.agendaItems
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
}

data class AgendaViewState(
    val type: AgendaState.StateType,
    val agendaItems: List<AgendaViewController.AgendaViewModel>
) : ViewState