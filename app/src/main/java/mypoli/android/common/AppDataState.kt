package mypoli.android.common

import mypoli.android.common.redux.Action
import mypoli.android.common.redux.Reducer
import mypoli.android.common.redux.State
import mypoli.android.player.Player

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/24/2018.
 */

sealed class DataLoadedAction : Action {
    data class PlayerLoaded(val player: Player) : DataLoadedAction()
}

data class AppDataState(
    val player: Player?
) : State

object AppDataReducer : Reducer<AppDataState, DataLoadedAction> {
    override fun reduce(state: AppDataState, action: DataLoadedAction) =
        when (action) {
            is DataLoadedAction.PlayerLoaded -> {
                state.copy(
                    player = action.player
                )
            }
        }

    override fun defaultState(): AppDataState {
        return AppDataState(
            null
        )
    }

}