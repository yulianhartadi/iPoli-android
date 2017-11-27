package io.ipoli.android.player.usecase

import io.ipoli.android.common.StreamingUseCase
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.Player
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.produce
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/15/17.
 */
class ListenForPlayerChangesUseCase(
    private val playerRepository: PlayerRepository,
    private val coroutineContext: CoroutineContext
) : StreamingUseCase<Unit, Player> {

    override fun execute(parameters: Unit) =
        transform(playerRepository.listen())

    private fun transform(dataChannel: ReceiveChannel<Player?>) = produce(coroutineContext) {
        dataChannel.consumeEach { player ->
            send(player!!)
        }
    }

}