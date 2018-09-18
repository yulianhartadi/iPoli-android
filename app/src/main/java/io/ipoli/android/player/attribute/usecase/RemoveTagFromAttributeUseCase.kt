package io.ipoli.android.player.attribute.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.tag.Tag

class RemoveTagFromAttributeUseCase(private val playerRepository: PlayerRepository) :
    UseCase<RemoveTagFromAttributeUseCase.Params, Player> {
    override fun execute(parameters: Params): Player {
        val p = playerRepository.find()
        requireNotNull(p)
        return playerRepository.save(
            p!!.removeTagFromAttribute(
                parameters.attribute,
                parameters.tag
            )
        )
    }

    data class Params(val attribute: Player.AttributeType, val tag: Tag)
}