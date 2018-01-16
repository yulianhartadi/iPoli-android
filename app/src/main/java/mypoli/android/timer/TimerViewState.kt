package mypoli.android.timer

import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.quest.Quest

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 6.01.18.
 */

sealed class TimerIntent : Intent {
    data class LoadData(val questId: String) : TimerIntent()
    data class QuestChanged(val quest: Quest) : TimerIntent()
    object Start : TimerIntent()
    object Stop : TimerIntent()
}

data class TimerViewState(
    val type: StateType,
    val showTimerTypeSwitch: Boolean = false,
    val timerType: TimerType = TimerType.COUNTDOWN,
    val maxProgress: Int = 0,
    val currentProgress: Int = 0
) : ViewState {

    enum class StateType {
        LOADING,
        RUNNING,
        STOPPED
    }

    enum class TimerType {
        COUNTDOWN,
        POMODORO
    }
}