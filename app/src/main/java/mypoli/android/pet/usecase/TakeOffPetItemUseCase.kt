package mypoli.android.pet.usecase

import mypoli.android.common.UseCase
import mypoli.android.pet.PetItem
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/26/17.
 */
class TakeOffPetItemUseCase(private val playerRepository: PlayerRepository) :
    UseCase<TakeOffPetItemUseCase.Params, Player> {
    override fun execute(parameters: Params): Player {
        val player = playerRepository.find()
        requireNotNull(player)

        val equipment = player!!.pet.equipment
        val item = parameters.item
        val newEquipment = when (item) {
            equipment.hat -> equipment.copy(hat = null)
            equipment.mask -> equipment.copy(mask = null)
            equipment.bodyArmor -> equipment.copy(bodyArmor = null)
            else -> throw IllegalArgumentException("Item not equipped $item")
        }

        return playerRepository.save(
            player.copy(
                pet = player.pet.copy(
                    equipment = newEquipment
                )
            )
        )
    }

    data class Params(val item: PetItem)
}