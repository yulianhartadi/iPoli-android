package io.ipoli.android.tag.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.tag.Tag

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/07/2018.
 */
class AddQuestCountToTagUseCase(private val questRepository: QuestRepository) :
    UseCase<AddQuestCountToTagUseCase.Params, Tag> {

    override fun execute(parameters: Params): Tag {
        val tag = parameters.tag
        val questCount = questRepository.findCountForTag(tag.id)
        return tag.copy(
            questCount = questCount
        )
    }

    data class Params(val tag: Tag)
}