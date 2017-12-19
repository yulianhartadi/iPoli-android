package mypoli.android.player.usecase

import mypoli.android.common.Reward
import mypoli.android.common.UseCase
import mypoli.android.player.LevelUpScheduler
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository
import mypoli.android.quest.Quest

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 28.11.17.
 */
open class RewardPlayerUseCase(
    private val playerRepository: PlayerRepository,
    private val levelUpScheduler: LevelUpScheduler
) : UseCase<Reward, Player> {

    override fun execute(parameters: Reward): Player {
        val reward = parameters
        requireNotNull(reward.experience)
        requireNotNull(reward.coins)
        val player = playerRepository.find()
        requireNotNull(player)

        val newPet = player!!.pet.rewardFor(reward)

        val inventory = player.inventory.let {
            val bounty = reward.bounty
            if (bounty is Quest.Bounty.Food) {
                it.addFood(bounty.food)
            } else {
                it
            }
        }

        val newPlayer = player
            .addExperience(reward.experience)
            .addCoins(reward.coins)
            .copy(
                pet = newPet,
                inventory = inventory
            )
        if (newPlayer.level != player.level) {
            levelUpScheduler.schedule()
        }
        playerRepository.save(newPlayer)
        return newPlayer
    }
}