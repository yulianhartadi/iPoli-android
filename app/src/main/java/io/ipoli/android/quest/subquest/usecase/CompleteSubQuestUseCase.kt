package io.ipoli.android.quest.subquest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/31/2018.
 */
class CompleteSubQuestUseCase(private val questRepository: QuestRepository) :
    UseCase<CompleteSubQuestUseCase.Params, Quest> {

    override fun execute(parameters: Params): Quest {
        val subQuestIndex = parameters.subQuestIndex

        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)

        val sqs = quest!!.subQuests.toMutableList()

        sqs[subQuestIndex] = sqs[subQuestIndex].copy(
            completedAtDate = LocalDate.now(),
            completedAtTime = Time.now()
        )

        return questRepository.save(
            quest.copy(
                subQuests = sqs
            )
        )
    }

    data class Params(val subQuestIndex: Int, val questId: String)
}