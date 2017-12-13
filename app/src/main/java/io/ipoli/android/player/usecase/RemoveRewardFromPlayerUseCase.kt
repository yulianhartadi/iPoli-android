package io.ipoli.android.player.usecase

import io.ipoli.android.common.Reward
import io.ipoli.android.common.UseCase
import io.ipoli.android.player.LevelDownScheduler
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 29.11.17.
 */
open class RemoveRewardFromPlayerUseCase(
    private val playerRepository: PlayerRepository,
    private val levelDownScheduler: LevelDownScheduler
) : UseCase<Reward, Player> {
    override fun execute(parameters: Reward): Player {
        val player = playerRepository.find()
        requireNotNull(player)
        val newPet = player!!.pet.removeReward(parameters)
        val newPlayer = player
            .removeExperience(parameters.experience)
            .removeCoins(parameters.coins)
            .copy(pet = newPet)
        playerRepository.save(newPlayer)
        if (player.level != newPlayer.level) {
            levelDownScheduler.schedule()
        }
        return newPlayer
    }

}