package io.ipoli.android.player.usecase

import io.ipoli.android.achievement.usecase.UnlockAchievementsUseCase
import io.ipoli.android.achievement.usecase.UpdatePlayerStatsUseCase
import io.ipoli.android.common.Reward
import io.ipoli.android.common.UseCase
import io.ipoli.android.player.LevelDownScheduler
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 29.11.17.
 */
open class RemoveRewardFromPlayerUseCase(
    private val playerRepository: PlayerRepository,
    private val levelDownScheduler: LevelDownScheduler,
    private val unlockAchievementsUseCase: UnlockAchievementsUseCase
) : UseCase<RemoveRewardFromPlayerUseCase.Params, Player> {
    override fun execute(parameters: Params): Player {
        val player = parameters.player ?: playerRepository.find()
        requireNotNull(player)
        val reward = parameters.reward
        val newPlayer = playerRepository.save(player!!.removeReward(reward))
        if (player.level != newPlayer.level) {
            levelDownScheduler.schedule()
        }
        unlockAchievementsUseCase.execute(
            UnlockAchievementsUseCase.Params(
                player = newPlayer,
                eventType = UpdatePlayerStatsUseCase.Params.EventType.ExperienceDecreased(reward.experience.toLong())
            )
        )
        return newPlayer
    }

    data class Params(val reward: Reward, val player: Player? = null)

}