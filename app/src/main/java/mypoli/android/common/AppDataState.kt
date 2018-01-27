package mypoli.android.common

import mypoli.android.common.redux.Action
import mypoli.android.common.redux.State
import mypoli.android.player.Player

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/24/2018.
 */

sealed class DataLoadedAction : Action {
    data class PlayerChanged(val player: Player) : DataLoadedAction()
}

data class AppDataState(
    val player: Player?
) : State

object AppDataReducer : AppStateReducer<AppDataState> {

    override fun reduce(state: AppState, action: Action) =
        when (action) {
            is DataLoadedAction.PlayerChanged -> {
                state.appDataState.copy(
                    player = action.player
                )
            }
            else -> {
                state.appDataState
            }
        }

    override fun defaultState(): AppDataState {
        return AppDataState(
            null
        )
    }

}