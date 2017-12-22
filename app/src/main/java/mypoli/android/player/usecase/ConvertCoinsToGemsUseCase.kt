package mypoli.android.player.usecase

import mypoli.android.Constants
import mypoli.android.common.UseCase
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository

/**
 * Created by vini on 12/21/17.
 */
class ConvertCoinsToGemsUseCase(private val playerRepository: PlayerRepository) : UseCase<ConvertCoinsToGemsUseCase.Params, ConvertCoinsToGemsUseCase.Result> {

    override fun execute(parameters: Params): Result {
        val gems = parameters.gems
        val player = playerRepository.find()
        requireNotNull(player)

        val gemsPrice = gems * Constants.GEM_COINS_PRICE

        if (player!!.coins < gemsPrice) {
            return Result.TooExpensive
        }

        val newPlayer = player.copy(
            coins = player.coins - gemsPrice,
            gems = player.gems + gems
        )

        return Result.GemsConverted(playerRepository.save(newPlayer))
    }

    data class Params(val gems: Int)

    sealed class Result {
        data class GemsConverted(val player: Player) : Result()
        object TooExpensive : Result()
    }

}