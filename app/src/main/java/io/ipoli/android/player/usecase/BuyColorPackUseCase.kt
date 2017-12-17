package io.ipoli.android.player.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.quest.ColorPack

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 16.12.17.
 */
class BuyColorPackUseCase(private val playerRepository: PlayerRepository) : UseCase<BuyColorPackUseCase.Params, BuyColorPackUseCase.Result> {

    override fun execute(parameters: BuyColorPackUseCase.Params): BuyColorPackUseCase.Result {

        val colorPack = parameters.colorPack
        val player = playerRepository.find()
        requireNotNull(player)
        require(!player!!.hasColorPack(colorPack))

        if (player.coins < colorPack.price) {
            return BuyColorPackUseCase.Result.TooExpensive
        }

        val newPlayer = player.copy(
            coins = player.coins - colorPack.price,
            inventory = player.inventory.addColorPack(colorPack)
        )

        return BuyColorPackUseCase.Result.ColorPackBought(playerRepository.save(newPlayer))
    }

    data class Params(val colorPack: ColorPack)

    sealed class Result {
        data class ColorPackBought(val player: Player) : Result()
        object TooExpensive : Result()
    }
}
