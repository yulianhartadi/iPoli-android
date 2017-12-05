package io.ipoli.android.player.usecase

import io.ipoli.android.common.StreamingUseCase
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
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