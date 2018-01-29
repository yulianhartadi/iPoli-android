package mypoli.android.common

import mypoli.android.challenge.category.list.ChallengeListForCategoryReducer
import mypoli.android.challenge.category.list.ChallengeListForCategoryState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.Reducer
import mypoli.android.common.redux.State
import mypoli.android.pet.store.PetStoreReducer
import mypoli.android.pet.store.PetStoreState
import mypoli.android.quest.agenda.AgendaReducer
import mypoli.android.quest.agenda.AgendaState
import mypoli.android.quest.calendar.CalendarReducer
import mypoli.android.quest.calendar.CalendarState

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/23/2018.
 */

interface AppStateReducer<out S : State> : Reducer<AppState, S>

sealed class LoadDataAction : Action {
    object All : LoadDataAction()
}

data class AppState(
    val appDataState: AppDataState,
    val calendarState: CalendarState,
    val agendaState: AgendaState,
    val petStoreState: PetStoreState,
    val challengeListForCategoryState: ChallengeListForCategoryState
) : State

object AppReducer : Reducer<AppState, AppState> {

    override fun reduce(state: AppState, action: Action) = state.copy(
        calendarState = CalendarReducer.reduce(state, action),
        agendaState = AgendaReducer.reduce(state, action),
        appDataState = AppDataReducer.reduce(state, action),
        petStoreState = PetStoreReducer.reduce(state, action)
    )

    override fun defaultState() =
        AppState(
            appDataState = AppDataReducer.defaultState(),
            calendarState = CalendarReducer.defaultState(),
            agendaState = AgendaReducer.defaultState(),
            petStoreState = PetStoreReducer.defaultState(),
            challengeListForCategoryState = ChallengeListForCategoryReducer.defaultState()
        )
}