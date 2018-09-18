package io.ipoli.android.tag.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.tag.persistence.TagRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/07/2018.
 */
class RemoveTagUseCase(
    private val tagRepository: TagRepository,
    private val playerRepository: PlayerRepository
) : UseCase<RemoveTagUseCase.Params, Unit> {

    override fun execute(parameters: Params) {
        tagRepository.remove(parameters.tagId)
        val player = playerRepository.find()!!
        val attrs = player.attributes.map {
            it.key to it.value.copy(
                tags = it.value.tags.filter { t -> t.id != parameters.tagId }
            )
        }.toMap()
        if (attrs != player.attributes) {
            playerRepository.save(player.copy(attributes = attrs))
        }
    }

    data class Params(val tagId: String)
}