package io.ipoli.android.pet.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.pet.Food
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 12/1/17.
 */

sealed class Result {
    data class PetFed(val player: Player) : Result()
    object NotEnoughCoins : Result()
}

data class Parameters(val food: Food)

class FeedPetUseCase(private val playerRepository: PlayerRepository) : UseCase<Parameters, Result> {
    override fun execute(parameters: Parameters): Result {
        val food = parameters.food
        val player = playerRepository.find()
        requireNotNull(player)

        if (player!!.inventory.hasFood(food)) {
            val newPlayer = player.copy(
                inventory = player.inventory.removeFood(food)
            )
            return Result.PetFed(playerRepository.save(newPlayer))
        }

        if (player.coins >= food.price) {
            val newPlayer = player.copy(
                coins = player.coins - food.price
            )
            return Result.PetFed(playerRepository.save(newPlayer))
        }

        return Result.NotEnoughCoins
    }

}