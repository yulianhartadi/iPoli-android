package io.ipoli.android.quest.subquest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/31/2018.
 */
class ReorderSubQuestUseCase(private val questRepository: QuestRepository) :
    UseCase<ReorderSubQuestUseCase.Params, Quest> {
    override fun execute(parameters: Params): Quest {
        val oldPos = parameters.oldPosition
        val newPos = parameters.newPosition

        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)

        val sqs = quest!!.subQuests.toMutableList()

        val sq = sqs[oldPos]
        sqs.removeAt(oldPos)
        sqs.add(newPos, sq)

        return questRepository.save(
            quest.copy(
                subQuests = sqs
            )
        )
    }

    data class Params(val oldPosition: Int, val newPosition: Int, val questId: String)
}