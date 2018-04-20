package io.ipoli.android.quest.show.usecase

import io.ipoli.android.Constants
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.minutes
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.TimeRange
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.show.job.TimerCompleteScheduler
import io.ipoli.android.quest.usecase.CompleteQuestUseCase
import io.ipoli.android.quest.usecase.CompleteQuestUseCase.Params.WithQuest
import org.threeten.bp.Instant

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 1/18/18.
 */
class CompleteTimeRangeUseCase(
    private val questRepository: QuestRepository,
    private val splitDurationForPomodoroTimerUseCase: SplitDurationForPomodoroTimerUseCase,
    private val completeQuestUseCase: CompleteQuestUseCase,
    private val timerCompleteScheduler: TimerCompleteScheduler
) :
    UseCase<CompleteTimeRangeUseCase.Params, Quest> {

    override fun execute(parameters: Params): Quest {
        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)

        timerCompleteScheduler.cancelAll()

        if(!quest!!.hasTimer) {
            return completeQuestUseCase.execute(WithQuest(quest))
        }

        val time = parameters.time

        if (quest.hasCountDownTimer) {
            val newQuest = questRepository.save(endLastTimeRange(quest, time))
            return completeQuestUseCase.execute(WithQuest(newQuest))
        }

        val splitResult = splitDurationForPomodoroTimerUseCase
            .execute(SplitDurationForPomodoroTimerUseCase.Params(quest))

        if (splitResult == SplitDurationForPomodoroTimerUseCase.Result.DurationNotSplit) {
            val newQuest = if (quest.timeRanges.isNotEmpty()) {
                questRepository.save(endLastTimeRange(quest, time))
            } else quest

            return completeQuestUseCase.execute(WithQuest(newQuest))
        }

        val timeRanges =
            (splitResult as SplitDurationForPomodoroTimerUseCase.Result.DurationSplit).timeRanges

        if (timeRanges.size <= quest.timeRanges.size) {
            val newQuest = questRepository.save(endLastTimeRange(quest, time))
            return completeQuestUseCase.execute(WithQuest(newQuest))
        }

        val questWithEndedLastRange = endLastTimeRange(quest, time)
        val currentTimeRanges = questWithEndedLastRange.timeRanges.toMutableList()
        val lastRangeType = questWithEndedLastRange.timeRanges.last().type

        val newRangeDuration: Int
        val newRangeType = if (lastRangeType == TimeRange.Type.POMODORO_SHORT_BREAK) {
            newRangeDuration = Constants.DEFAULT_POMODORO_WORK_DURATION
            TimeRange.Type.POMODORO_WORK
        } else {
            newRangeDuration = if ((currentTimeRanges.size + 1) % 8 == 0) {
                Constants.DEFAULT_POMODORO_LONG_BREAK_DURATION
            } else {
                Constants.DEFAULT_POMODORO_BREAK_DURATION
            }
            TimeRange.Type.POMODORO_SHORT_BREAK
        }

        val newQuest = questWithEndedLastRange.copy(
            timeRanges = currentTimeRanges +
                TimeRange(
                    newRangeType,
                    newRangeDuration,
                    start = time
                )
        )

        timerCompleteScheduler.schedule(
            questId = quest.id,
            after = newRangeDuration.minutes.asSeconds
        )

        return questRepository.save(newQuest)
    }

    private fun endLastTimeRange(
        quest: Quest,
        time: Instant
    ): Quest {
        val lastTimeRange = quest.timeRanges.last().copy(
            end = time
        )
        return quest.copy(
            timeRanges = quest.timeRanges - quest.timeRanges.last() + lastTimeRange
        )
    }

    data class Params(
        val questId: String,
        val time: Instant = Instant.now()
    )
}