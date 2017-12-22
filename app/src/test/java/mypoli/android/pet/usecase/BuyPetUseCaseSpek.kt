package mypoli.android.pet.usecase

import mypoli.android.TestUtil
import mypoli.android.pet.Pet
import mypoli.android.pet.PetAvatar
import mypoli.android.player.Inventory
import mypoli.android.player.InventoryPet
import mypoli.android.player.Player
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 12/6/17.
 */
class BuyPetUseCaseSpek : Spek({
    describe("BuyPetUseCase") {
        fun executeUseCase(player: Player, pet: PetAvatar) =
            BuyPetUseCase(TestUtil.playerRepoMock(player)).execute(pet)

        it("should require not bought pet") {
            val pet = Pet(
                "",
                avatar = PetAvatar.ELEPHANT
            )

            val player = TestUtil.player().copy(
                pet = pet,
                inventory = Inventory(pets = setOf(InventoryPet.fromPet(pet)))
            )
            val exec = { executeUseCase(player, pet.avatar) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should not buy when not enough gems") {
            val player = TestUtil.player().copy(
                gems = PetAvatar.ELEPHANT.gemPrice - 1,
                inventory = Inventory()
            )
            val result = executeUseCase(player, PetAvatar.ELEPHANT)
            result.`should be`(BuyPetUseCase.Result.TooExpensive)
        }

        it("should buy pet") {
            val player = TestUtil.player().copy(
                gems = PetAvatar.ELEPHANT.gemPrice,
                inventory = Inventory()
            )
            val result = executeUseCase(player, PetAvatar.ELEPHANT)
            result.`should be instance of`(BuyPetUseCase.Result.PetBought::class)
            val newPlayer = (result as BuyPetUseCase.Result.PetBought).player
            newPlayer.gems.`should be equal to`(player.gems - PetAvatar.ELEPHANT.gemPrice)
            newPlayer.hasPet(PetAvatar.ELEPHANT).`should be true`()
        }
    }
})