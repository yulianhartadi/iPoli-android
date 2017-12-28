package mypoli.android.player.usecase

import mypoli.android.common.Reward
import mypoli.android.common.UseCase
import mypoli.android.player.LevelDownScheduler
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
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