package io.ipoli.android.player.usecase

import kotlinx.coroutines.experimental.channels.map
import io.ipoli.android.common.StreamingUseCase
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/15/17.
 */
class ListenForPlayerChangesUseCase(
    private val playerRepository: PlayerRepository
) : StreamingUseCase<Unit, Player> {

    override fun execute(parameters: Unit) =
        playerRepository.listen().map {
            it!!
        }
}