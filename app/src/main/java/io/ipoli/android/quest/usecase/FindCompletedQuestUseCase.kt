package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/15/17.
 */
class FindCompletedQuestUseCase(private val questRepository: QuestRepository) : UseCase<String, Quest> {
    override fun execute(parameters: String): Quest {
        val quest = questRepository.findById(parameters)!!

        requireNotNull(quest.completedAtDate)
        requireNotNull(quest.completedAtTime)
        requireNotNull(quest.experience)

        return quest
    }

}