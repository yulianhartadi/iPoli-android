package mypoli.android.store.avatar.usecase

import mypoli.android.common.UseCase
import mypoli.android.player.Player
import mypoli.android.player.data.Avatar
import mypoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/25/2018.
 */
class ChangeAvatarUseCase(private val playerRepository: PlayerRepository) :
    UseCase<ChangeAvatarUseCase.Params, Player> {

    override fun execute(parameters: Params): Player {
        val player = playerRepository.find()
        requireNotNull(player)
        val avatar = parameters.avatar
        require(player!!.inventory.hasAvatar(avatar))

        return playerRepository.save(
            player.copy(
                avatar = avatar
            )
        )
    }

    data class Params(val avatar: Avatar)
}