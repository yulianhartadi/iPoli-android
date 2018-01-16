package mypoli.android.quest.usecase

import mypoli.android.Constants
import mypoli.android.common.UseCase
import mypoli.android.quest.Quest
import mypoli.android.quest.TimeRange

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/16/18.
 */
class SplitDurationForPomodoroTimerUseCase : UseCase<SplitDurationForPomodoroTimerUseCase.Params, SplitDurationForPomodoroTimerUseCase.Result> {

    override fun execute(parameters: Params): Result {
        val quest = parameters.quest
        val duration = quest.duration
        val pomodoroDuration = Constants.DEFAULT_POMODORO_WORK_DURATION + Constants.DEFAULT_POMODORO_BREAK_DURATION

        if (duration < pomodoroDuration) {
            return Result.DurationNotSplit
        }

        val ranges = mutableListOf<TimeRange>()
        var minutes = 0
        while (minutes < duration) {
            val range = if (ranges.isEmpty() || ranges[ranges.size - 1].type == TimeRange.Type.BREAK) {
                val workDuration = Math.min(Constants.DEFAULT_POMODORO_WORK_DURATION, duration - minutes)
                TimeRange(TimeRange.Type.WORK, workDuration)
            } else {
                val breakDuration = if((ranges.size + 1) % 8 == 0) {
                    Math.min(Constants.DEFAULT_POMODORO_LONG_BREAK_DURATION, duration - minutes)
                } else {
                    Constants.DEFAULT_POMODORO_BREAK_DURATION
                }
                TimeRange(TimeRange.Type.BREAK, breakDuration)
            }
            minutes += range.duration
            ranges.add(range)
        }

        return Result.DurationSplit(ranges)
    }

    data class Params(val quest: Quest)
    sealed class Result {
        data class DurationSplit(val timeRanges: List<TimeRange>) : Result()
        object DurationNotSplit : Result()
    }
}