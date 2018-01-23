package mypoli.android.timer.usecase

import mypoli.android.common.UseCase
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/18/18.
 */
open class CancelTimerUseCase(private val questRepository: QuestRepository) :
    UseCase<CancelTimerUseCase.Params, Quest> {

    override fun execute(parameters: Params): Quest {
        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)

        if (quest!!.hasCountDownTimer) {
            return questRepository.save(quest.copy(actualStart = null))
        }

        require(quest.hasPomodoroTimer)

        return questRepository.save(
            quest.copy(
                pomodoroTimeRanges = quest.pomodoroTimeRanges.toMutableList() - quest.pomodoroTimeRanges.last()
            )
        )
    }

    data class Params(val questId: String)
}