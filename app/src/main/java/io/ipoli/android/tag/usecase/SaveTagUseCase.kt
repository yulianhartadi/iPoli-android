package io.ipoli.android.tag.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.tag.Tag
import io.ipoli.android.tag.persistence.TagRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/04/2018.
 */
class SaveTagUseCase(private val tagRepository: TagRepository) :
    UseCase<SaveTagUseCase.Params, Tag> {

    override fun execute(parameters: Params): Tag {
        require(parameters.name.isNotBlank())

        return tagRepository.save(
            Tag(
                id = parameters.id ?: "",
                name = parameters.name,
                color = parameters.color,
                icon = parameters.icon,
                isFavorite = parameters.isFavorite
            )
        )

    }

    data class Params(
        val id: String?,
        val name: String,
        val icon: Icon?,
        val color: Color,
        val isFavorite: Boolean
    )
}