package mypoli.android.common

import mypoli.android.auth.AuthReducer
import mypoli.android.auth.AuthState
import mypoli.android.challenge.category.list.ChallengeListForCategoryReducer
import mypoli.android.challenge.category.list.ChallengeListForCategoryState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.Reducer
import mypoli.android.common.redux.State
import mypoli.android.pet.store.PetStoreReducer
import mypoli.android.pet.store.PetStoreState
import mypoli.android.quest.schedule.ScheduleReducer
import mypoli.android.quest.schedule.ScheduleState
import mypoli.android.quest.schedule.agenda.AgendaReducer
import mypoli.android.quest.schedule.agenda.AgendaState
import mypoli.android.quest.schedule.calendar.CalendarReducer
import mypoli.android.quest.schedule.calendar.CalendarState
import mypoli.android.repeatingquest.list.RepeatingQuestListReducer
import mypoli.android.repeatingquest.list.RepeatingQuestListState

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/23/2018.
 */

interface AppStateReducer<out S : State> : Reducer<AppState, S>

sealed class LoadDataAction : Action {
    object All : LoadDataAction()
    data class ChangePlayer(val oldPlayerId : String) : LoadDataAction()
}

data class AppState(
    val appDataState: AppDataState,
    val scheduleState: ScheduleState,
    val calendarState: CalendarState,
    val agendaState: AgendaState,
    val petStoreState: PetStoreState,
    val challengeListForCategoryState: ChallengeListForCategoryState,
    val authState: AuthState,
    val repeatingQuestListState: RepeatingQuestListState
) : State

object AppReducer : Reducer<AppState, AppState> {

    override fun reduce(state: AppState, action: Action) = state.copy(
        appDataState = AppDataReducer.reduce(state, action),
        scheduleState = ScheduleReducer.reduce(state, action),
        calendarState = CalendarReducer.reduce(state, action),
        agendaState = AgendaReducer.reduce(state, action),
        petStoreState = PetStoreReducer.reduce(state, action),
        authState = AuthReducer.reduce(state, action),
        repeatingQuestListState = RepeatingQuestListReducer.reduce(state, action)
    )

    override fun defaultState() =
        AppState(
            appDataState = AppDataReducer.defaultState(),
            scheduleState = ScheduleReducer.defaultState(),
            calendarState = CalendarReducer.defaultState(),
            agendaState = AgendaReducer.defaultState(),
            petStoreState = PetStoreReducer.defaultState(),
            challengeListForCategoryState = ChallengeListForCategoryReducer.defaultState(),
            authState = AuthReducer.defaultState(),
            repeatingQuestListState = RepeatingQuestListReducer.defaultState()
        )
}