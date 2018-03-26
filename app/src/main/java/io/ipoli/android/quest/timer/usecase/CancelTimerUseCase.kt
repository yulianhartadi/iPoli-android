package io.ipoli.android.quest.timer.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.timer.job.TimerCompleteScheduler

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 1/18/18.
 */
open class CancelTimerUseCase(
    private val questRepository: QuestRepository,
    private val completeScheduler: TimerCompleteScheduler
) : UseCase<CancelTimerUseCase.Params, Quest> {

    override fun execute(parameters: Params): Quest {
        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)

        completeScheduler.cancelAll()

        if (quest!!.hasCountDownTimer) {
            return questRepository.save(quest.copy(timeRanges = listOf()))
        }

        require(quest.hasPomodoroTimer)

        return questRepository.save(
            quest.copy(
                timeRanges = quest.timeRanges.toMutableList() - quest.timeRanges.last()
            )
        )
    }

    data class Params(val questId: String)
}