package io.ipoli.android.quest.subquest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.subquest.SubQuest

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/31/2018.
 */
class AddSubQuestUseCase(private val questRepository: QuestRepository) :
    UseCase<AddSubQuestUseCase.Params, Quest> {

    override fun execute(parameters: Params): Quest {
        val name = parameters.name

        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)

        val sqs = quest!!.subQuests.toMutableList()

        sqs.add(
            SubQuest(
                name = name,
                completedAtDate = null,
                completedAtTime = null
            )
        )

        return questRepository.save(
            quest.copy(
                subQuests = sqs
            )
        )
    }

    data class Params(val name: String, val questId: String)
}