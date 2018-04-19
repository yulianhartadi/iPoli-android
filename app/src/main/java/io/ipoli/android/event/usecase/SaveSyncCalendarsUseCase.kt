package io.ipoli.android.event.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.PlayerRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/03/2018.
 */
class SaveSyncCalendarsUseCase(private val playerRepository: PlayerRepository) :
    UseCase<SaveSyncCalendarsUseCase.Params, Player> {

    override fun execute(parameters: Params): Player {
        val p = playerRepository.find()
        requireNotNull(p)
        val prefs = p!!.preferences
        return playerRepository.save(
            p.updatePreferences(
                prefs.copy(
                    syncCalendarIds = parameters.calendarIds
                )
            )
        )
    }

    data class Params(val calendarIds: Set<String>)
}