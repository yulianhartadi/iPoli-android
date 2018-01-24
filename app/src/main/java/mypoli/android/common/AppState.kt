package mypoli.android.common

import mypoli.android.challenge.category.list.ChallengeListForCategoryAction
import mypoli.android.challenge.category.list.ChallengeListForCategoryReducer
import mypoli.android.challenge.category.list.ChallengeListForCategoryState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.Reducer
import mypoli.android.common.redux.State
import mypoli.android.pet.store.PetStoreAction
import mypoli.android.pet.store.PetStoreReducer
import mypoli.android.pet.store.PetStoreState
import mypoli.android.quest.calendar.CalendarAction
import mypoli.android.quest.calendar.CalendarReducer
import mypoli.android.quest.calendar.CalendarState

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/23/2018.
 */

sealed class LoadDataAction : Action {
    object All : LoadDataAction()
}

data class AppState(
    val appDataState: AppDataState,
    val calendarState: CalendarState,
    val petStoreState: PetStoreState,
    val challengeListForCategoryState: ChallengeListForCategoryState
) : State

object AppReducer : Reducer<AppState, Action> {

    override fun reduce(state: AppState, action: Action) =
        when (action) {
            is CalendarAction -> state.copy(
                calendarState = CalendarReducer.reduce(state.calendarState, action)
            )
            is DataLoadedAction -> state.copy(
                appDataState = AppDataReducer.reduce(state.appDataState, action)
            )
            is PetStoreAction -> state.copy(
                petStoreState = PetStoreReducer.reduce(state, action)
            )
            is ChallengeListForCategoryAction -> {
                state.copy(
                    challengeListForCategoryState = ChallengeListForCategoryReducer.reduce(
                        state,
                        action
                    )
                )
            }
            else -> state
        }

    override fun defaultState() =
        AppState(
            appDataState = AppDataReducer.defaultState(),
            calendarState = CalendarReducer.defaultState(),
            petStoreState = PetStoreReducer.defaultState(),
            challengeListForCategoryState = ChallengeListForCategoryReducer.defaultState()
        )
}