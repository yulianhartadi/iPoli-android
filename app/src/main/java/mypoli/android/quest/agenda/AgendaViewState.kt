package mypoli.android.quest.agenda

import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.State
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/26/2018.
 */

sealed class AgendaAction : Action {

}

data class AgendaState(val type: StateType, val topDate: LocalDate) : State {
    enum class StateType {
        DATA_CHANGED
    }
}

data class AgendaViewState(
    val type: AgendaState.StateType,
    val topDate: LocalDate,
    val quests: List<AgendaViewController.QuestViewModel>
) : ViewState