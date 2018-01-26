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

data class AgendaState(val topDate: LocalDate) : State

data class AgendaViewState(val topDate: LocalDate) : ViewState