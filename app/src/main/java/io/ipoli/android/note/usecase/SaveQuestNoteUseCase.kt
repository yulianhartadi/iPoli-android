package io.ipoli.android.note.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 4/1/18.
 */
class SaveQuestNoteUseCase(private val questRepository: QuestRepository) :
    UseCase<SaveQuestNoteUseCase.Params, Quest> {

    override fun execute(parameters: Params): Quest {
        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)
        val note = parameters.note?.trim()
        return questRepository.save(
            quest!!.copy(
                note = if (note != null && note.isNotEmpty()) note else null
            )
        )
    }

    data class Params(val questId: String, val note: String?)
}