package io.ipoli.android.player.usecase

import io.ipoli.android.common.StreamingUseCase
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map
import kotlinx.coroutines.experimental.launch

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/15/17.
 */
class ListenForPlayerChangesUseCase(
    private val playerRepository: PlayerRepository
) : StreamingUseCase<Unit, Player> {

    override fun execute(parameters: Unit): ReceiveChannel<Player> {
        val c = Channel<Player>()
        launch(UI) {
            val listenChannel = Channel<Player?>()
            playerRepository.listen(listenChannel).map {
                c.send(it!!)
            }
        }
        return c
    }
}