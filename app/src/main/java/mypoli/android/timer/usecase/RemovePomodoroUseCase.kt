package mypoli.android.timer.usecase

import mypoli.android.Constants
import mypoli.android.common.UseCase
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/19/18.
 */
class RemovePomodoroUseCase(
    private val questRepository: QuestRepository,
    private val splitDurationForPomodoroTimerUseCase: SplitDurationForPomodoroTimerUseCase
) :
    UseCase<RemovePomodoroUseCase.Params, Quest> {

    override fun execute(parameters: Params): Quest {
        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)

        val splitResult = splitDurationForPomodoroTimerUseCase.execute(
            SplitDurationForPomodoroTimerUseCase.Params(quest!!)
        )

        if (splitResult == SplitDurationForPomodoroTimerUseCase.Result.DurationNotSplit) {
            return quest
        }

        val timeRanges =
            (splitResult as SplitDurationForPomodoroTimerUseCase.Result.DurationSplit).timeRanges
        val pomodoroDuration = if (timeRanges.size % 8 == 0) {
            Constants.DEFAULT_POMODORO_WORK_DURATION + Constants.DEFAULT_POMODORO_LONG_BREAK_DURATION
        } else {
            Constants.DEFAULT_POMODORO_WORK_DURATION + Constants.DEFAULT_POMODORO_BREAK_DURATION
        }

        val newDuration = Math.max(
            quest.duration - pomodoroDuration,
            MIN_QUEST_POMODORO_DURATION
        )

        if (quest.pomodoroTimeRanges.isNotEmpty() && newDuration < quest.actualDuration.asMinutes.intValue) {
            return quest
        }

        return questRepository.save(
            quest.copy(
                duration = newDuration
            )
        )
    }

    companion object {
        const val MIN_QUEST_POMODORO_DURATION =
            Constants.DEFAULT_POMODORO_WORK_DURATION + Constants.DEFAULT_POMODORO_BREAK_DURATION
    }

    data class Params(val questId: String)
}