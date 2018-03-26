package mypoli.android.store.powerup.usecase

import mypoli.android.common.UseCase
import mypoli.android.player.Player
import mypoli.android.player.persistence.PlayerRepository
import mypoli.android.store.powerup.PowerUp
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/19/2018.
 */
open class EnableAllPowerUpsUseCase(private val playerRepository: PlayerRepository) :
    UseCase<EnableAllPowerUpsUseCase.Params, Player> {

    override fun execute(parameters: EnableAllPowerUpsUseCase.Params): Player {
        val p = playerRepository.find()
        require(p != null)

        val powerUps = PowerUp.Type.values().map {
            PowerUp.fromType(it, parameters.expirationDate)
        }

        return playerRepository.save(
            p!!.copy(
                inventory = p.inventory.setPowerUps(powerUps)
            )
        )
    }

    data class Params(
        val expirationDate: LocalDate
    )
}