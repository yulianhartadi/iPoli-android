package io.ipoli.android.quest.timer

import io.ipoli.android.Constants
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.datetime.*
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.TimeRange
import io.ipoli.android.quest.timer.sideeffect.TimerStartedAction
import io.ipoli.android.quest.timer.view.formatter.TimerFormatter
import org.threeten.bp.Instant

/**
 * Created by Venelin Valkov <venelin@io.ipoli.io>
 * on 6.01.18.
 */

sealed class TimerAction : Action {
    data class Load(val questId: String) : TimerAction()
    data class Changed(val quest: Quest) : TimerAction()
    object Start : TimerAction()
    object Stop : TimerAction()
    object Tick : TimerAction()
    object CompletePomodoro : TimerAction()
    object ShowCountDownTimer : TimerAction()
    object ShowPomodoroTimer : TimerAction()
    object CompleteQuest : TimerAction()
    object AddPomodoro : TimerAction()
    object RemovePomodoro : TimerAction()
}

object TimerReducer : BaseViewStateReducer<TimerViewState>() {

    override val stateKey = key<TimerViewState>()

    override fun reduce(state: AppState, subState: TimerViewState, action: Action) =
        when (action) {

            is DataLoadedAction.QuestChanged -> {
                if (action.quest.isCompleted) {
                    subState.copy(
                        type = TimerViewState.StateType.QUEST_COMPLETED
                    )
                } else {
                    createQuestChangedState(subState.copy(quest = action.quest))
                }
            }

            is TimerStartedAction -> {
                val type =
                    if (action.otherTimerStopped)
                        TimerViewState.StateType.TIMER_REPLACED
                    else
                        TimerViewState.StateType.TIMER_STARTED
                subState.copy(
                    type = type
                )
            }

            TimerAction.Stop -> {
                subState.copy(
                    type = TimerViewState.StateType.TIMER_STOPPED
                )
            }

            TimerAction.Tick -> {
                val remainingTime = subState.remainingTime!! - 1.seconds
                val shouldShowCompletePomodoroButton =
                    subState.timerType == TimerViewState.TimerType.POMODORO && remainingTime < 0.seconds
                subState.copy(
                    type = TimerViewState.StateType.RUNNING,
                    timerProgress = subState.timerProgress + 1,
                    timerLabel = formatDuration(remainingTime),
                    remainingTime = remainingTime,
                    showCompletePomodoroButton = shouldShowCompletePomodoroButton
                )
            }

            TimerAction.CompletePomodoro -> {
                subState.copy(
                    type = TimerViewState.StateType.TIMER_STOPPED
                )
            }

            TimerAction.ShowPomodoroTimer -> {
                createStateForInitialPomodoroTimer(subState, subState.quest!!)
            }

            TimerAction.ShowCountDownTimer -> {
                createStateForInitialCountDownTimer(subState, subState.quest!!)
            }

            TimerAction.CompleteQuest -> {
                subState.copy(
                    type = TimerViewState.StateType.TIMER_STOPPED
                )
            }

            TimerAction.AddPomodoro -> {
                subState.copy(
                    type = TimerViewState.StateType.POMODORO_ADDED
                )
            }

            TimerAction.RemovePomodoro -> {
                subState.copy(
                    type = TimerViewState.StateType.POMODORO_REMOVED
                )
            }

            else -> subState
        }

    override fun defaultState() = TimerViewState(TimerViewState.StateType.LOADING)

    private fun formatDuration(duration: Duration<Second>): String {
        return if (duration >= 0.seconds) {
            TimerFormatter.format(duration.millisValue)
        } else {
            "+" + TimerFormatter.format(Math.abs(duration.millisValue))
        }
    }

    private fun createQuestChangedState(
        state: TimerViewState
    ): TimerViewState {
        val quest = state.quest
        if (quest!!.hasCountDownTimer) {
            return createStateForRunningCountdownTimer(quest, state)
        }

        if (quest.hasPomodoroTimer) {
            return createStateForRunningPomodoroTimer(quest, state)
        }

        return if (quest.duration < MIN_INITIAL_POMODORO_TIMER_DURATION) {
            createStateForInitialCountDownTimer(state, quest)
        } else {
            createStateForInitialPomodoroTimer(state, quest)
        }
    }

    private fun createStateForInitialPomodoroTimer(
        state: TimerViewState,
        quest: Quest
    ) =
        state.copy(
            type = TimerViewState.StateType.SHOW_POMODORO,
            questName = quest.name,
            timerType = TimerViewState.TimerType.POMODORO,
            showTimerTypeSwitch = true,
            pomodoroProgress = quest.timeRangesToComplete.map { createPomodoroProgress(it) },
            timerLabel = formatDuration(Constants.DEFAULT_POMODORO_WORK_DURATION.minutes.asSeconds),
            remainingTime = Constants.DEFAULT_POMODORO_WORK_DURATION.minutes.asSeconds,
            currentProgressIndicator = 0,
            timerProgress = 0,
            maxTimerProgress = Constants.DEFAULT_POMODORO_WORK_DURATION.minutes.asSeconds.intValue
        )

    private fun createStateForInitialCountDownTimer(
        state: TimerViewState,
        quest: Quest
    ) =
        state.copy(
            type = TimerViewState.StateType.SHOW_COUNTDOWN,
            questName = quest.name,
            timerType = TimerViewState.TimerType.COUNTDOWN,
            showTimerTypeSwitch = quest.duration >= MIN_POMODORO_TIMER_DURATION,
            timerLabel = formatDuration(quest.duration.minutes.asSeconds),
            remainingTime = quest.duration.minutes.asSeconds,
            timerProgress = 0,
            maxTimerProgress = quest.duration.minutes.asSeconds.intValue
        )

    private fun createStateForRunningPomodoroTimer(
        quest: Quest,
        state: TimerViewState
    ): TimerViewState {
        val currentProgressIndicator =
            findCurrentProgressIndicator(quest.timeRangesToComplete)

        val currentTimeRange = if (currentProgressIndicator >= 0) {
            quest.timeRanges.last()
        } else {
            quest.timeRangesToComplete[quest.timeRanges.size]
        }

        val duration = currentTimeRange.duration
        val passed: Duration<Millisecond> = if (currentTimeRange.start != null)
            (Instant.now() - currentTimeRange.start).milliseconds
        else
            0.milliseconds

        val remainingTime = duration.minutes - passed

        val type =
            if (quest.timeRanges.last().end == null) TimerViewState.StateType.RESUMED
            else TimerViewState.StateType.SHOW_POMODORO

        return state.copy(
            type = type,
            questName = quest.name,
            timerType = TimerViewState.TimerType.POMODORO,
            showTimerTypeSwitch = false,
            pomodoroProgress = quest.timeRangesToComplete.map { createPomodoroProgress(it) },
            timerLabel = formatDuration(remainingTime.asSeconds),
            remainingTime = remainingTime.asSeconds,
            currentProgressIndicator = currentProgressIndicator,
            timerProgress = passed.asSeconds.intValue,
            maxTimerProgress = duration.minutes.asSeconds.intValue
        )
    }

    private fun createStateForRunningCountdownTimer(
        quest: Quest,
        state: TimerViewState
    ): TimerViewState {

        val passed = Instant.now() - quest.actualStart!!
        val remainingTime =
            quest.duration.minutes - passed.milliseconds
        return state.copy(
            type = TimerViewState.StateType.RESUMED,
            questName = quest.name,
            timerType = TimerViewState.TimerType.COUNTDOWN,
            showTimerTypeSwitch = false,
            timerLabel = formatDuration(remainingTime.asSeconds),
            remainingTime = remainingTime.asSeconds,
            timerProgress = passed.milliseconds.asSeconds.intValue,
            maxTimerProgress = quest.duration.minutes.asSeconds.intValue
        )
    }

    private fun findCurrentProgressIndicator(timeRanges: List<TimeRange>): Int =
        timeRanges.indexOfFirst { it.start != null && it.end == null }

    private fun createPomodoroProgress(timeRange: TimeRange): PomodoroProgress {
        return when (timeRange.type) {
            TimeRange.Type.POMODORO_SHORT_BREAK -> {
                if (timeRange.end != null) {
                    PomodoroProgress.COMPLETE_SHORT_BREAK
                } else {
                    PomodoroProgress.INCOMPLETE_SHORT_BREAK
                }
            }

            TimeRange.Type.POMODORO_LONG_BREAK -> {
                if (timeRange.end != null) {
                    PomodoroProgress.COMPLETE_LONG_BREAK
                } else {
                    PomodoroProgress.INCOMPLETE_LONG_BREAK
                }
            }

            TimeRange.Type.POMODORO_WORK -> {
                if (timeRange.end != null) {
                    PomodoroProgress.COMPLETE_WORK
                } else {
                    PomodoroProgress.INCOMPLETE_WORK
                }
            }
            else -> PomodoroProgress.COMPLETE_WORK
        }
    }

    const val MIN_INITIAL_POMODORO_TIMER_DURATION =
        Constants.DEFAULT_POMODORO_WORK_DURATION * 2 + Constants.DEFAULT_POMODORO_BREAK_DURATION * 2

    const val MIN_POMODORO_TIMER_DURATION =
        Constants.DEFAULT_POMODORO_WORK_DURATION + Constants.DEFAULT_POMODORO_BREAK_DURATION

}

data class TimerViewState(
    val type: StateType,
    val quest: Quest? = null,
    val showTimerTypeSwitch: Boolean = false,
    val timerLabel: String = "",
    val remainingTime: Duration<Second>? = null,
    val timerType: TimerType = TimerType.COUNTDOWN,
    val questName: String = "",
    val timerProgress: Int = 0,
    val maxTimerProgress: Int = 0,
    val pomodoroProgress: List<PomodoroProgress> = listOf(),
    val currentProgressIndicator: Int = 0,
    val showCompletePomodoroButton: Boolean = false
) : ViewState {

    enum class StateType {
        LOADING,
        RESUMED,
        SHOW_POMODORO,
        SHOW_COUNTDOWN,
        TIMER_STARTED,
        TIMER_REPLACED,
        TIMER_STOPPED,
        RUNNING,
        POMODORO_ADDED,
        POMODORO_REMOVED,
        QUEST_COMPLETED
    }

    enum class TimerType {
        COUNTDOWN,
        POMODORO
    }
}