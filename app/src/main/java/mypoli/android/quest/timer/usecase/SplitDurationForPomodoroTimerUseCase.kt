package mypoli.android.quest.timer.usecase

import mypoli.android.Constants
import mypoli.android.common.UseCase
import mypoli.android.quest.Quest
import mypoli.android.quest.TimeRange

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 1/16/18.
 */
open class SplitDurationForPomodoroTimerUseCase :
    UseCase<SplitDurationForPomodoroTimerUseCase.Params, SplitDurationForPomodoroTimerUseCase.Result> {

    override fun execute(parameters: Params): Result {
        val quest = parameters.quest
        val duration = quest.duration
        val pomodoroDuration =
            Constants.DEFAULT_POMODORO_WORK_DURATION + Constants.DEFAULT_POMODORO_BREAK_DURATION

        if (!quest.hasPomodoroTimer && duration < pomodoroDuration) {
            return Result.DurationNotSplit
        }

        val ranges = quest.timeRanges.toMutableList()
        var scheduledDuration = ranges.sumBy { it.duration }
        while (scheduledDuration < duration) {
            val range =
                if (ranges.isEmpty() || ranges.last().type == TimeRange.Type.POMODORO_SHORT_BREAK
                    || ranges.last().type == TimeRange.Type.POMODORO_LONG_BREAK) {
                    val workDuration =
                        Math.min(
                            Constants.DEFAULT_POMODORO_WORK_DURATION,
                            duration - scheduledDuration
                        )
                    TimeRange(TimeRange.Type.POMODORO_WORK, workDuration)
                } else {
                    val (breakType, breakDuration) = if ((ranges.size + 1) % 8 == 0) {
                        val longDuration = Math.min(
                            Constants.DEFAULT_POMODORO_LONG_BREAK_DURATION,
                            duration - scheduledDuration
                        )
                        Pair(TimeRange.Type.POMODORO_LONG_BREAK, longDuration)
                    } else {
                        Pair(
                            TimeRange.Type.POMODORO_SHORT_BREAK,
                            Constants.DEFAULT_POMODORO_BREAK_DURATION
                        )
                    }
                    TimeRange(breakType, breakDuration)
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