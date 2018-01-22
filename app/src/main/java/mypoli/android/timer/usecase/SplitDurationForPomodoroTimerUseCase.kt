package mypoli.android.timer.usecase

import mypoli.android.Constants
import mypoli.android.common.UseCase
import mypoli.android.quest.Quest
import mypoli.android.quest.TimeRange

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/16/18.
 */
class SplitDurationForPomodoroTimerUseCase :
    UseCase<SplitDurationForPomodoroTimerUseCase.Params, SplitDurationForPomodoroTimerUseCase.Result> {

    override fun execute(parameters: Params): Result {
        val quest = parameters.quest
        val duration = quest.duration
        val pomodoroDuration =
            Constants.DEFAULT_POMODORO_WORK_DURATION + Constants.DEFAULT_POMODORO_BREAK_DURATION

        if (quest.pomodoroTimeRanges.isEmpty() && duration < pomodoroDuration) {
            return Result.DurationNotSplit
        }

        val ranges = quest.pomodoroTimeRanges.toMutableList()
        var scheduledDuration = ranges.sumBy { it.duration }
        while (scheduledDuration < duration) {
            val range = if (ranges.isEmpty() || ranges.last().type == TimeRange.Type.BREAK) {
                val workDuration =
                    Math.min(Constants.DEFAULT_POMODORO_WORK_DURATION, duration - scheduledDuration)
                TimeRange(TimeRange.Type.WORK, workDuration)
            } else {
                val breakDuration = if ((ranges.size + 1) % 8 == 0) {
                    Math.min(
                        Constants.DEFAULT_POMODORO_LONG_BREAK_DURATION,
                        duration - scheduledDuration
                    )
                } else {
                    Constants.DEFAULT_POMODORO_BREAK_DURATION
                }
                TimeRange(TimeRange.Type.BREAK, breakDuration)
            }
            scheduledDuration += range.duration
            ranges.add(range)
        }

        return Result.DurationSplit(
            ranges
        )
    }

    data class Params(val quest: Quest)
    sealed class Result {
        data class DurationSplit(val timeRanges: List<TimeRange>) : Result()
        object DurationNotSplit : Result()
    }
}