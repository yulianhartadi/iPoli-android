package mypoli.android.common

import mypoli.android.common.redux.Action
import mypoli.android.common.redux.State
import mypoli.android.player.Player
import mypoli.android.quest.Quest
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/24/2018.
 */

sealed class DataLoadedAction : Action {
    data class PlayerChanged(val player: Player) : DataLoadedAction()
    data class TodayQuestsChanged(val quests: List<Quest>) : DataLoadedAction()
}

data class AppDataState(
    val today: LocalDate,
    val player: Player?,
    val todayQuests: List<Quest>
) : State

object AppDataReducer : AppStateReducer<AppDataState> {

    override fun reduce(state: AppState, action: Action) =
        when (action) {
            is DataLoadedAction.PlayerChanged -> {
                state.appDataState.copy(
                    player = action.player
                )
            }
            is DataLoadedAction.TodayQuestsChanged -> {
                state.appDataState.copy(
                    todayQuests = action.quests
                )
            }
            else -> {
                state.appDataState
            }
        }

    override fun defaultState(): AppDataState {
        return AppDataState(
            today = LocalDate.now(),
            player = null,
            todayQuests = listOf()
        )
    }

}