package io.ipoli.android.home

import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.quest.Player

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/15/17.
 */

sealed class HomeIntent : Intent

object LoadDataIntent : HomeIntent()

data class PlayerChangedIntent(val player: Player) : HomeIntent()

data class HomeViewState(
    val type: StateType = StateType.DATA_LOADED,
    val progress: Int = 0,
    val maxProgress: Int = 0,
    val level: Int = 0
) : ViewState {
    enum class StateType {
        LOADING, DATA_LOADED, LEVEL_CHANGED, XP_CHANGED
    }
}