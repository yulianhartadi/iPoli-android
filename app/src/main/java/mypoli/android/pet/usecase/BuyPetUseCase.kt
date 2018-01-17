package mypoli.android.pet.usecase

import mypoli.android.Constants
import mypoli.android.common.UseCase
import mypoli.android.pet.Pet
import mypoli.android.pet.PetAvatar
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/6/17.
 */
class BuyPetUseCase(private val playerRepository: PlayerRepository) :
    UseCase<PetAvatar, BuyPetUseCase.Result> {
    override fun execute(parameters: PetAvatar): Result {
        val avatar = parameters
        val player = playerRepository.find()
        requireNotNull(player)
        require(!player!!.hasPet(avatar))

        if (player.gems < avatar.gemPrice) {
            return BuyPetUseCase.Result.TooExpensive
        }

        val newPlayer = player.copy(
            gems = player.gems - avatar.gemPrice,
            inventory = player.inventory.addPet(
                Pet(
                    name = Constants.DEFAULT_PET_NAME,
                    avatar = avatar
                )
            )
        )

        return Result.PetBought(playerRepository.save(newPlayer))
    }

    sealed class Result {
        data class PetBought(val player: Player) : Result()
        object TooExpensive : Result()
    }
}