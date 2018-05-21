package io.ipoli.android.tag.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.tag.Tag
import io.ipoli.android.tag.persistence.TagRepository

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 4/5/18.
 */
class FavoriteTagUseCase(
    private val tagRepository: TagRepository,
    private val saveTagUseCase: SaveTagUseCase
) : UseCase<FavoriteTagUseCase.Params, Tag> {

    override fun execute(parameters: Params): Tag {
        val tag = when (parameters) {
            is Params.WithTag -> parameters.tag
            is Params.WithTagId -> tagRepository.findById(parameters.tagId)!!
        }

        return saveTagUseCase.execute(
            SaveTagUseCase.Params(
                id = tag.id,
                name = tag.name,
                icon = tag.icon,
                color = tag.color,
                isFavorite = true
            )
        )
    }

    sealed class Params {
        data class WithTag(val tag: Tag) : Params()
        data class WithTagId(val tagId: String) : Params()
    }
}