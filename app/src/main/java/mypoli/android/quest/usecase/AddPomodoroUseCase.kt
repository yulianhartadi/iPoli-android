package mypoli.android.quest.usecase

import mypoli.android.Constants
import mypoli.android.common.UseCase
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/19/18.
 */
class AddPomodoroUseCase(
    private val questRepository: QuestRepository,
    private val splitDurationForPomodoroTimerUseCase: SplitDurationForPomodoroTimerUseCase
) :
    UseCase<AddPomodoroUseCase.Params, Quest> {

    override fun execute(parameters: Params): Quest {
        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)

        val splitResult = splitDurationForPomodoroTimerUseCase.execute(
            SplitDurationForPomodoroTimerUseCase.Params(quest!!)
        )

        val questDuration = Math.max(quest.duration, quest.actualDuration)

        val pomodoroDuration: Int

        if (splitResult == SplitDurationForPomodoroTimerUseCase.Result.DurationNotSplit) {
            pomodoroDuration = Constants.DEFAULT_POMODORO_WORK_DURATION +
                Constants.DEFAULT_POMODORO_BREAK_DURATION
        } else {
            val timeRanges =
                (splitResult as SplitDurationForPomodoroTimerUseCase.Result.DurationSplit).timeRanges
            pomodoroDuration = if ((timeRanges.size + 2) % 8 == 0) {
                Constants.DEFAULT_POMODORO_WORK_DURATION + Constants.DEFAULT_POMODORO_LONG_BREAK_DURATION
            } else {
                Constants.DEFAULT_POMODORO_WORK_DURATION + Constants.DEFAULT_POMODORO_BREAK_DURATION
            }
        }

        val newQuest = quest.copy(
            duration = questDuration + pomodoroDuration
        )

        return questRepository.save(newQuest)
    }

    data class Params(val questId: String)
}