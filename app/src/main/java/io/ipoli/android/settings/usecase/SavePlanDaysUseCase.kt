package io.ipoli.android.settings.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.player.persistence.PlayerRepository
import org.threeten.bp.DayOfWeek

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/17/18.
 */
class SavePlanDaysUseCase(
    private val playerRepository: PlayerRepository
) : UseCase<SavePlanDaysUseCase.Params, Unit> {

    override fun execute(parameters: Params) {
        val player = playerRepository.find()
        requireNotNull(player)

        playerRepository.save(
            player!!.updatePreferences(
                player.preferences.copy(
                    planDays = parameters.days
                )
            )
        )
    }

    data class Params(val days: Set<DayOfWeek>)
}