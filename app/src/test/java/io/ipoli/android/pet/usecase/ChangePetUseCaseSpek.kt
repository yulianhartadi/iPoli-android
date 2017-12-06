package io.ipoli.android.pet.usecase

import io.ipoli.android.TestUtil
import io.ipoli.android.pet.Pet
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.player.Inventory
import io.ipoli.android.player.InventoryPet
import io.ipoli.android.player.Player
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 12/6/17.
 */
class ChangePetUseCaseSpek : Spek({
    describe("ChangePetUseCase") {

        fun executeUseCase(player: Player, pet: PetAvatar): Player {
            return ChangePetUseCase(TestUtil.playerRepoMock(player)).execute(pet)
        }

        it("should require pet in inventory") {
            val player = TestUtil.player().copy(
                inventory = Inventory(pets = listOf(
                    InventoryPet("", PetAvatar.CHICKEN)
                ))
            )
            val exec = { executeUseCase(player, PetAvatar.ELEPHANT) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should change pet") {
            val currentPet = Pet(
                name = "",
                avatar = PetAvatar.ELEPHANT
            )
            val newPet = Pet(
                name = "",
                avatar = PetAvatar.CHICKEN
            )
            val player = TestUtil.player().copy(
                pet = currentPet,
                inventory = Inventory(
                    pets = listOf(
                        InventoryPet.fromPet(currentPet),
                        InventoryPet.fromPet(newPet)
                    )
                )
            )

            val newPlayer = executeUseCase(player, newPet.avatar)
            newPlayer.pet.avatar.`should be`(newPet.avatar)
        }
    }
})