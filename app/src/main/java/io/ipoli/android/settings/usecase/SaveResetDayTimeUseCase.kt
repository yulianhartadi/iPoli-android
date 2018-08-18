package io.ipoli.android.settings.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.job.ResetDayScheduler
import io.ipoli.android.player.persistence.PlayerRepository

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/17/18.
 */
class SaveResetDayTimeUseCase(
    private val playerRepository: PlayerRepository,
    private val resetDayScheduler: ResetDayScheduler
) : UseCase<SaveResetDayTimeUseCase.Params, Unit> {

    override fun execute(parameters: Params) {
        val player = playerRepository.find()
        requireNotNull(player)
        playerRepository.save(
            player!!.updatePreferences(
                player.preferences.copy(
                    resetDayTime = parameters.time
                )
            )
        )
        resetDayScheduler.schedule()
    }

    data class Params(val time: Time)
}