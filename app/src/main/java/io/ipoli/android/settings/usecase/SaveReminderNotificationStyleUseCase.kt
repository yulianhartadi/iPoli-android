package io.ipoli.android.settings.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.persistence.PlayerRepository

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/17/18.
 */
class SaveReminderNotificationStyleUseCase(
    private val playerRepository: PlayerRepository
) : UseCase<SaveReminderNotificationStyleUseCase.Params, Unit> {

    override fun execute(parameters: Params) {
        val player = playerRepository.find()
        requireNotNull(player)

        playerRepository.save(
            player!!.updatePreferences(
                player.preferences.copy(
                    reminderNotificationStyle = parameters.style
                )
            )
        )
    }

    data class Params(val style: Player.Preferences.NotificationStyle)
}