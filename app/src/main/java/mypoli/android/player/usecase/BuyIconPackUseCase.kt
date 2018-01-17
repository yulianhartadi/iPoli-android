package mypoli.android.player.usecase

import mypoli.android.common.UseCase
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository
import mypoli.android.quest.IconPack

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 15.12.17.
 */
class BuyIconPackUseCase(private val playerRepository: PlayerRepository) :
    UseCase<BuyIconPackUseCase.Params, BuyIconPackUseCase.Result> {

    override fun execute(parameters: Params): Result {

        val iconPack = parameters.iconPack
        val player = playerRepository.find()
        requireNotNull(player)
        require(!player!!.hasIconPack(iconPack))

        if (player.gems < iconPack.gemPrice) {
            return Result.TooExpensive
        }

        val newPlayer = player.copy(
            gems = player.gems - iconPack.gemPrice,
            inventory = player.inventory.addIconPack(iconPack)
        )

        return Result.IconPackBought(playerRepository.save(newPlayer))
    }

    data class Params(val iconPack: IconPack)

    sealed class Result {
        data class IconPackBought(val player: Player) : Result()
        object TooExpensive : Result()
    }
}