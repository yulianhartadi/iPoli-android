package mypoli.android.pet.usecase

import mypoli.android.Constants
import mypoli.android.common.UseCase
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/5/17.
 */
class RevivePetUseCase(private val playerRepository: PlayerRepository) : UseCase<Unit, RevivePetUseCase.Result> {
    override fun execute(parameters: Unit): Result {
        val player = playerRepository.find()
        requireNotNull(player)
        val pet = player!!.pet
        require(pet.isDead)

        if (player.gems < Constants.REVIVE_PET_GEM_PRICE) {
            return Result.TooExpensive
        }

        val newPlayer = player.copy(
            gems = player.gems - Constants.REVIVE_PET_GEM_PRICE,
            pet = player.pet.setHealthAndMoodPoints(
                Constants.DEFAULT_PET_HP,
                Constants.DEFAULT_PET_MP
            )
        )

        return Result.PetRevived(playerRepository.save(newPlayer))
    }

    sealed class Result {
        data class PetRevived(val player: Player) : Result()
        object TooExpensive : Result()
    }
}