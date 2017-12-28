package mypoli.android.store.usecase

import mypoli.android.Constants
import mypoli.android.common.UseCase
import mypoli.android.pet.Pet
import mypoli.android.pet.PetAvatar
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository
import mypoli.android.store.purchase.GemPack
import mypoli.android.store.purchase.GemPackType

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/28/17.
 */
class PurchaseGemPackUseCase(private val playerRepository: PlayerRepository) : UseCase<PurchaseGemPackUseCase.Params, PurchaseGemPackUseCase.Result> {

    override fun execute(parameters: Params): Result {
        val player = playerRepository.find()
        requireNotNull(player)

        val gemPack = parameters.gemPack

        val hasUnlockedPet = !player!!.hasPet(PetAvatar.DOG) && gemPack.type == GemPackType.SMART

        val newPlayer = player.copy(
            gems = player.gems + gemPack.gems,
            inventory = if (hasUnlockedPet)
                player.inventory.addPet(
                    Pet(
                        name = Constants.DEFAULT_PET_NAME,
                        avatar = PetAvatar.DOG
                    )
                )
            else player.inventory

        )

        return Result(playerRepository.save(newPlayer), hasUnlockedPet)
    }

    data class Params(val gemPack: GemPack)
    data class Result(val player: Player, val hasUnlockedPet: Boolean)
}