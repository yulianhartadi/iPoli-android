package io.ipoli.android.quest.schedule.agenda

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.datetime.isBetween

import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.quest.schedule.agenda.usecase.CreateAgendaItemsUseCase.AgendaItem
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/26/2018.
 */

sealed class AgendaAction : Action {
    data class Load(val startDate: LocalDate) : AgendaAction() {
        override fun toMap() = mapOf("startDate" to startDate)
    }

    data class LoadBefore(val itemPosition: Int) : AgendaAction() {
        override fun toMap() = mapOf("itemPosition" to itemPosition)
    }

    data class LoadAfter(val itemPosition: Int) : AgendaAction() {
        override fun toMap() = mapOf("itemPosition" to itemPosition)
    }

    data class CompleteQuest(val itemPosition: Int) : AgendaAction() {
        override fun toMap() = mapOf("itemPosition" to itemPosition)
    }

    data class UndoCompleteQuest(val itemPosition: Int) : AgendaAction() {
        override fun toMap() = mapOf("itemPosition" to itemPosition)
    }

    data class FirstVisibleItemChanged(val itemPosition: Int) : AgendaAction() {
        override fun toMap() = mapOf("itemPosition" to itemPosition)
    }
}

object AgendaReducer : BaseViewStateReducer<AgendaViewState>() {

    override val stateKey = key<AgendaViewState>()

    override fun reduce(
        state: AppState,
        subState: AgendaViewState,
        action: Action
    ): AgendaViewState {
        return when (action) {

            is DataLoadedAction.AgendaItemsChanged -> {
                subState.copy(
                    type = AgendaViewState.StateType.DATA_CHANGED,
                    agendaItems = action.agendaItems,
                    scrollToPosition = findItemPositionToScrollTo(
                        action.currentAgendaItemDate,
                        action.agendaItems
                    )
                )
            }
            is AgendaAction.LoadBefore -> {
                subState.copy(
                    type = AgendaViewState.StateType.SHOW_TOP_LOADER
                )
            }
            is AgendaAction.LoadAfter -> {
                subState.copy(
                    type = AgendaViewState.StateType.SHOW_BOTTOM_LOADER
                )
            }
            is AgendaAction.FirstVisibleItemChanged -> {
                subState.copy(
                    type = AgendaViewState.StateType.IDLE
                )
            }
            else -> subState

        }
    }

    private fun findItemPositionToScrollTo(
        date: LocalDate?,
        agendaItems: List<AgendaItem>
    ) = date?.let {
        val currentAgendaItemDate = it
        val index = agendaItems.indexOfLast {
            when (it) {
                is AgendaItem.Date ->
                    it.startDate() == currentAgendaItemDate
                is AgendaItem.Week ->
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

    override fun defaultState() = AgendaViewState(
        type = AgendaViewState.StateType.LOADING,
        agendaItems = listOf(),
        scrollToPosition = null
    )

    const val ITEMS_BEFORE_COUNT = 25
    const val ITEMS_AFTER_COUNT = 35
}

data class AgendaViewState(
    val type: AgendaViewState.StateType,
    val scrollToPosition: Int?,
    val agendaItems: List<AgendaItem>
) : BaseViewState() {

    enum class StateType {
        LOADING,
        DATA_CHANGED,
        SHOW_TOP_LOADER,
        SHOW_BOTTOM_LOADER,
        IDLE
    }

}