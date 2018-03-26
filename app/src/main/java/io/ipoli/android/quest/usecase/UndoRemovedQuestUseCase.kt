package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/20/17.
 */
class UndoRemovedQuestUseCase(private val questRepository: QuestRepository) :
    UseCase<String, Unit> {
    override fun execute(parameters: String) {
        val questId = parameters
        questRepository.undoRemove(questId)

    }

}