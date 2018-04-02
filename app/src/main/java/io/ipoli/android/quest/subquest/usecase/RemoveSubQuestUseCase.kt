package io.ipoli.android.quest.subquest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/31/2018.
 */
class RemoveSubQuestUseCase(private val questRepository: QuestRepository) :
    UseCase<RemoveSubQuestUseCase.Params, Quest> {

    override fun execute(parameters: Params): Quest {
        val index = parameters.subQuestIndex

        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)

        val sqs = quest!!.subQuests.toMutableList()
        sqs.removeAt(index)

        return questRepository.save(
            quest.copy(
                subQuests = sqs
            )
        )
    }

    data class Params(val subQuestIndex: Int, val questId: String)
}