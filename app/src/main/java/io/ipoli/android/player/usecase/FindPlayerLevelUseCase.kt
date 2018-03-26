package io.ipoli.android.player.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/15/17.
 */
class FindPlayerLevelUseCase(private val playerRepository: PlayerRepository) : UseCase<Unit, Int> {
    override fun execute(parameters: Unit): Int {
        val player = playerRepository.find()
        requireNotNull(player)
        return player!!.level
    }
}