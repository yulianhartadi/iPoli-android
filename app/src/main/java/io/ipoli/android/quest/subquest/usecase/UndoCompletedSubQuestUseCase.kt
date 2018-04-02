package io.ipoli.android.quest.subquest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/31/2018.
 */
class UndoCompletedSubQuestUseCase(private val questRepository: QuestRepository) :
    UseCase<UndoCompletedSubQuestUseCase.Params, Quest> {

    override fun execute(parameters: Params): Quest {
        val subQuestIndex = parameters.subQuestIndex

        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)

        val sqs = quest!!.subQuests.toMutableList()

        val editSubQuest = sqs[subQuestIndex]

        requireNotNull(editSubQuest.completedAtDate)
        requireNotNull(editSubQuest.completedAtTime)

        sqs[subQuestIndex] = editSubQuest.copy(
            completedAtDate = null,
            completedAtTime = null
        )

        return questRepository.save(
            quest.copy(
                subQuests = sqs
            )
        )
    }

    data class Params(val subQuestIndex: Int, val questId: String)
}