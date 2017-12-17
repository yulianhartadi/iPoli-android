package io.ipoli.android.pet.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.pet.Pet
import io.ipoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 12/7/17.
 */
class FindPetUseCase(private val playerRepository: PlayerRepository) : UseCase<Unit, Pet> {
    override fun execute(parameters: Unit) =
        playerRepository.find().let {
            requireNotNull(it)
            it!!.pet
        }
}