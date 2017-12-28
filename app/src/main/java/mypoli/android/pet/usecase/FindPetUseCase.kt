package mypoli.android.pet.usecase

import mypoli.android.common.UseCase
import mypoli.android.pet.Pet
import mypoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/7/17.
 */
class FindPetUseCase(private val playerRepository: PlayerRepository) : UseCase<Unit, Pet> {
    override fun execute(parameters: Unit) =
        playerRepository.find().let {
            requireNotNull(it)
            it!!.pet
        }
}