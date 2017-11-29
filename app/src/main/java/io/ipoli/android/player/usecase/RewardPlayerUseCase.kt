package io.ipoli.android.player.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.player.LevelUpScheduler
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.quest.Quest

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 28.11.17.
 */
open class RewardPlayerUseCase(
    private val playerRepository: PlayerRepository,
    private val levelUpScheduler: LevelUpScheduler
) : UseCase<Quest, Player> {
    override fun execute(parameters: Quest): Player {
        requireNotNull(parameters.experience)
        requireNotNull(parameters.coins)
        val player = playerRepository.find()
        requireNotNull(player)

        val newPet = player!!.pet.rewardFor(parameters)
        val newPlayer = player
            .addExperience(parameters.experience!!)
            .addCoins(parameters.coins!!)
            .copy(pet = newPet)
        if (newPlayer.level != player.level) {
            levelUpScheduler.schedule()
        }
        playerRepository.save(newPlayer)
        return newPlayer
    }
}