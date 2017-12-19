package mypoli.android.player.usecase

import mypoli.android.common.StreamingUseCase
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository
import kotlinx.coroutines.experimental.channels.map

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