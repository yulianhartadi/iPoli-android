package io.ipoli.android.pet.usecase

import io.ipoli.android.TestUtil.player
import io.ipoli.android.TestUtil.playerRepoMock
import io.ipoli.android.pet.Food
import io.ipoli.android.player.Inventory
import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 12/1/17.
 */
class FeedPetUseCaseSpek : Spek({
    describe("FeedPetUseCase") {

        it("should not feed pet when not enough coins") {
            val player = player().copy(
                coins = 0,
                inventory = Inventory()
            )

            val playerRepo = playerRepoMock(player)

            val result = FeedPetUseCase(playerRepo).execute(Parameters(Food.BANANA))
            result.`should be instance of`(Result.NotEnoughCoins::class)
        }

        it("should buy food and feed pet") {
            val player = player().copy(
                coins = Food.BANANA.price,
                inventory = Inventory()
            )

            val playerRepo = playerRepoMock(player)

            val result = FeedPetUseCase(playerRepo).execute(Parameters(Food.BANANA))
            result.`should be instance of`(Result.PetFed::class)
            (result as Result.PetFed).player.coins.`should be equal to`(0)
        }

        it("should use food from inventory") {
            val player = player().copy(
                coins = 10,
                inventory = Inventory(mapOf(Food.BANANA to 1))
            )

            val playerRepo = playerRepoMock(player)

            val result = FeedPetUseCase(playerRepo).execute(Parameters(Food.BANANA))
            result.`should be instance of`(Result.PetFed::class)
            val newPlayer = (result as Result.PetFed).player
            newPlayer.coins.`should be equal to`(player.coins)
            newPlayer.inventory.food.`should be empty`()
        }

        it("should buy new food and not use food from inventory") {
            val player = player().copy(
                coins = Food.APPLE.price,
                inventory = Inventory(mapOf(Food.BANANA to 1))
            )

            val playerRepo = playerRepoMock(player)

            val result = FeedPetUseCase(playerRepo).execute(Parameters(Food.APPLE))
            result.`should be instance of`(Result.PetFed::class)
            val newPlayer = (result as Result.PetFed).player
            newPlayer.inventory.`should equal`(player.inventory)
        }

    }
})