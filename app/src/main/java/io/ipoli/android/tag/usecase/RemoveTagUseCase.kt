package io.ipoli.android.tag.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.tag.persistence.TagRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/07/2018.
 */
class RemoveTagUseCase(
    private val tagRepository: TagRepository
) :
    UseCase<RemoveTagUseCase.Params, Unit> {

    override fun execute(parameters: Params) {
        tagRepository.remove(parameters.tagId)
    }

    data class Params(val tagId: String)
}