package io.ipoli.android.pet.usecase

import io.ipoli.android.common.StreamingUseCase
import io.ipoli.android.pet.Pet
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.produce
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 11/27/17.
 */
class ListenForPetChangesUseCase(
    private val playerRepository: PlayerRepository,
    private val coroutineContext: CoroutineContext
) : StreamingUseCase<Unit, Pet> {

    override fun execute(parameters: Unit) =
        transform(playerRepository.listen())

    private fun transform(dataChannel: ReceiveChannel<Player?>) = produce(coroutineContext) {
        dataChannel.consumeEach { player ->
            send(player!!.pet)
        }
    }

}