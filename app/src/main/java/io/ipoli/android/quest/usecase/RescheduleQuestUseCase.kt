package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate

class RescheduleQuestUseCase(private val questRepository: QuestRepository) :
    UseCase<RescheduleQuestUseCase.Params, Quest> {

    override fun execute(parameters: Params): Quest {
        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)

        return questRepository.save(
            quest!!.copy(
                scheduledDate = parameters.scheduledDate,
                originalScheduledDate = quest.scheduledDate ?: parameters.scheduledDate
            )
        )
    }

    data class Params(val questId: String, val scheduledDate: LocalDate)
}