package mypoli.android.timer

import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 6.01.18.
 */

sealed class TimerIntent : Intent {
    object Start : TimerIntent()
    object Stop : TimerIntent()
}

data class TimerViewState(
    val state: StateType,
    val maxProgress: Int = 0,
    val currentProgress: Int = 0
) : ViewState {
    enum class StateType {
        LOADING,
        RUNNING,
        STOPPED
    }
}