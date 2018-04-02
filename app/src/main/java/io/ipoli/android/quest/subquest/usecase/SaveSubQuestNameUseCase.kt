package io.ipoli.android.quest.subquest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/31/2018.
 */
class SaveSubQuestNameUseCase(private val questRepository: QuestRepository) :
    UseCase<SaveSubQuestNameUseCase.Params, Quest> {

    override fun execute(parameters: Params): Quest {
        val newName = parameters.newName
        val subQuestIndex = parameters.index

        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)

        val sqs = quest!!.subQuests.toMutableList()

        sqs[subQuestIndex] = sqs[subQuestIndex].copy(
            name = newName
        )



        return questRepository.save(
            quest.copy(
                subQuests = sqs
            )
        )
    }

    data class Params(val newName: String, val questId: String, val index: Int)
}