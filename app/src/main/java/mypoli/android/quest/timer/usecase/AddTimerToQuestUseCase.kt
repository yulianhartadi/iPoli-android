package mypoli.android.quest.timer.usecase

import mypoli.android.Constants
import mypoli.android.common.UseCase
import mypoli.android.common.datetime.minutes
import mypoli.android.quest.Quest
import mypoli.android.quest.TimeRange
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.quest.timer.job.TimerCompleteScheduler
import org.threeten.bp.Instant

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 1/22/18.
 */
class AddTimerToQuestUseCase(
    private val questRepository: QuestRepository,
    private val cancelTimerUseCase: CancelTimerUseCase,
    private val timerCompleteScheduler: TimerCompleteScheduler
) :
    UseCase<AddTimerToQuestUseCase.Params, AddTimerToQuestUseCase.Result> {

    override fun execute(parameters: Params): Result {
        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)

        val startedQuests = questRepository.findStartedQuests()
        require(startedQuests.size <= 1)
        val startedQuest = startedQuests.firstOrNull()

        if (startedQuest != null) {
            cancelTimerUseCase.execute(CancelTimerUseCase.Params(startedQuest.id))
        }

        val time = parameters.time
        if (!parameters.isPomodoro) {
            return addContDownTimer(quest, time, startedQuest)
        }

        return addPomodoroTimer(quest, time, startedQuest)
    }

    private fun addPomodoroTimer(
        quest: Quest?,
        time: Instant,
        startedQuest: Quest?
    ): Result {
        require(!quest!!.hasPomodoroTimer)

        timerCompleteScheduler.schedule(
            questId = quest.id,
            after = Constants.DEFAULT_POMODORO_WORK_DURATION.minutes.asSeconds
        )
        val newQuest = quest.copy(
            timeRanges = quest.timeRanges.toMutableList() +
                TimeRange(
                    type = TimeRange.Type.POMODORO_WORK,
                    duration = Constants.DEFAULT_POMODORO_WORK_DURATION,
                    start = time
                )
        )
        return Result(questRepository.save(newQuest), startedQuest != null)
    }

    private fun addContDownTimer(
        quest: Quest?,
        time: Instant,
        startedQuest: Quest?
    ): Result {
        require(!quest!!.hasCountDownTimer)
        timerCompleteScheduler.schedule(
            questId = quest.id,
            after = quest.duration.minutes.asSeconds
        )
        return Result(
            questRepository.save(
                quest.copy(
                    timeRanges = listOf(
                        TimeRange(
                            TimeRange.Type.COUNTDOWN,
                            quest.duration,
                            time
                        )
                    )
                )
            ),
            startedQuest != null
        )
    }

    data class Result(val quest: Quest, val otherTimerStopped: Boolean)

    data class Params(
        val questId: String,
        val isPomodoro: Boolean,
        val time: Instant = Instant.now()
    )
}