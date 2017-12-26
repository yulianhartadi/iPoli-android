package mypoli.android.pet.usecase

import mypoli.android.common.UseCase
import mypoli.android.pet.PetItem
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/13/17.
 */
class BuyPetItemUseCase(private val playerRepository: PlayerRepository) : UseCase<BuyPetItemUseCase.Params, BuyPetItemUseCase.Result> {

    override fun execute(parameters: Params): Result {
        val item = parameters.item
        val player = playerRepository.find()
        requireNotNull(player)
        val petAvatar = player!!.pet.avatar
        require(!player.inventory.getPet(petAvatar).hasItem(item))

        if (player.gems < item.gemPrice) {
            return Result.TooExpensive
        }

        val newPlayer = player.copy(
            gems = player.gems - item.gemPrice,
            inventory = player.inventory.addPetItem(item to petAvatar)
        )

        return Result.ItemBought(playerRepository.save(newPlayer))
    }


    data class Params(val item: PetItem)

    sealed class Result {
        data class ItemBought(val player: Player) : Result()
        object TooExpensive : Result()
    }
}