package mypoli.android.store.powerup.usecase

import mypoli.android.TestUtil
import mypoli.android.player.Player
import mypoli.android.store.powerup.PowerUp
import mypoli.android.store.powerup.usecase.EnableAllPowerUpsUseCase.Params
import org.amshove.kluent.`should be`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/19/2018.
 */
class EnableAllPowerUpsUseCaseSpek : Spek({

    describe("EnableAllPowerUpsUseCase") {

        fun executeUseCase(
            expirationDate: LocalDate,
            existingPowerUps: List<PowerUp> = listOf()
        ): Player {
            val player = TestUtil.player().copy(
                inventory = TestUtil.player().inventory.setPowerUps(existingPowerUps)
            )
            return EnableAllPowerUpsUseCase(TestUtil.playerRepoMock(player))
                .execute(Params(expirationDate))
        }

        fun checkPowerUpsExpirationDate(
            powerUps: List<PowerUp>,
            expirationDate: LocalDate?
        ) {
            powerUps.forEach {
                it.expirationDate.`should be`(expirationDate)
            }
        }

        it("should enable all powerUps until tomorrow when no powerUps are present") {
            val expirationDate = LocalDate.now().plusDays(1)
            val powerUps = executeUseCase(expirationDate).inventory.powerUps
            powerUps.size.`should be`(PowerUp.Type.values().size)
            checkPowerUpsExpirationDate(powerUps, expirationDate)
        }

        it("should override existing powerUps") {
            val expirationDate = LocalDate.now().plusDays(1)
            val powerUps = executeUseCase(
                expirationDate,
                existingPowerUps = listOf(
                    PowerUp.fromType(
                        PowerUp.Type.TIMER,
                        expirationDate.plusDays(1)
                    )
                )
            ).inventory.powerUps
            powerUps.size.`should be`(PowerUp.Type.values().size)
            checkPowerUpsExpirationDate(powerUps, expirationDate)
        }
    }
})

