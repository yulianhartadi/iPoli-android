package mypoli.android.pet.usecase

import mypoli.android.common.UseCase
import mypoli.android.pet.PetAvatar
import mypoli.android.pet.PetEquipment
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/6/17.
 */
class ChangePetUseCase(private val playerRepository: PlayerRepository) : UseCase<PetAvatar, Player> {
    override fun execute(parameters: PetAvatar): Player {
        val pet = parameters
        val player = playerRepository.find()
        requireNotNull(player)
        require(player!!.hasPet(pet))

        val inventoryPet = player.inventory.getPet(pet)
        val newPlayer = player.copy(
            pet = player.pet.copy(
                name = inventoryPet.name,
                avatar = inventoryPet.avatar,
                equipment = PetEquipment()
            )
        )

        return playerRepository.save(newPlayer)
    }

}