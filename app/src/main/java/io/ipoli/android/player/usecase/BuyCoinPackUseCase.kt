package io.ipoli.android.player.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.quest.IconPack
import io.ipoli.android.theme.usecase.BuyThemeUseCase

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 15.12.17.
 */
class BuyCoinPackUseCase(private val playerRepository: PlayerRepository) : UseCase<BuyCoinPackUseCase.Params, BuyCoinPackUseCase.Result> {

    override fun execute(parameters: Params): Result {

        val iconPack = parameters.iconPack
        val player = playerRepository.find()
        requireNotNull(player)
        require(!player!!.hasIconPack(iconPack))

        if (player.coins < iconPack.price) {
            return Result.TooExpensive
        }

        val newPlayer = player.copy(
            coins = player.coins - iconPack.price,
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