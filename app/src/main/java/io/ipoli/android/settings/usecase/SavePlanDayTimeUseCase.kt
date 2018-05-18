package io.ipoli.android.settings.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.planday.job.PlanDayScheduler
import io.ipoli.android.player.persistence.PlayerRepository

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/17/18.
 */
class SavePlanDayTimeUseCase(
    private val playerRepository: PlayerRepository,
    private val planDayScheduler: PlanDayScheduler
) : UseCase<SavePlanDayTimeUseCase.Params, Unit> {

    override fun execute(parameters: Params) {
        val player = playerRepository.find()
        requireNotNull(player)

        playerRepository.save(
            player!!.updatePreferences(
                player.preferences.copy(
                    planDayTime = parameters.time
                )
            )
        )
        planDayScheduler.scheduleForNextTime()
    }

    data class Params(val time: Time)
}