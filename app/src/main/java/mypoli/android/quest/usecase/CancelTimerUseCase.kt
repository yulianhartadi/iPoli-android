package mypoli.android.quest.usecase

import mypoli.android.common.UseCase
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/18/18.
 */
class CancelTimerUseCase(private val questRepository: QuestRepository) :
    UseCase<CancelTimerUseCase.Params, Quest> {

    override fun execute(parameters: Params): Quest {
        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)

        if (quest!!.actualStart != null) {
            return questRepository.save(quest.copy(actualStart = null))
        }

        require(quest.pomodoroTimeRanges.isNotEmpty())

        return questRepository.save(
            quest.copy(
                pomodoroTimeRanges = quest.pomodoroTimeRanges.toMutableList() - quest.pomodoroTimeRanges.last()
            )
        )
    }

    data class Params(val questId: String)
}