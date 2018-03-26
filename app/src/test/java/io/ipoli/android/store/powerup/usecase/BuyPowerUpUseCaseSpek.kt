package io.ipoli.android.store.powerup.usecase

import io.ipoli.android.Constants
import io.ipoli.android.TestUtil
import io.ipoli.android.store.powerup.PowerUp
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/16/2018.
 */
class BuyPowerUpUseCaseSpek : Spek({

    describe("BuyPowerUpUseCase") {

        val defaultDuration = 30

        fun executeUseCase(
            powerUp: PowerUp.Type,
            durationDays: Int = defaultDuration,
            playerCoins: Int = Constants.DEFAULT_PLAYER_COINS
        ) =
            BuyPowerUpUseCase(
                TestUtil.playerRepoMock(
                    TestUtil.player().copy(coins = playerCoins)
                )
            ).execute(
                BuyPowerUpUseCase.Params(
                    powerUp,
                    durationDays
                )
            )

        it("should be too expensive when not enough coins in Player") {
            executeUseCase(PowerUp.Type.TIMER).`should be`(BuyPowerUpUseCase.Result.TooExpensive)
        }

        it("should enable PowerUp for 30 days") {
            val powerUpPrice = PowerUp.Type.TIMER.coinPrice
            val res = executeUseCase(
                PowerUp.Type.TIMER,
                durationDays = defaultDuration,
                playerCoins = powerUpPrice
            )
            res.`should be instance of`(BuyPowerUpUseCase.Result.Bought::class)
            val p = (res as BuyPowerUpUseCase.Result.Bought).player
            p.coins.`should be`(0)
            p.inventory.isPowerUpEnabled(PowerUp.Type.TIMER).`should be true`()
            p.inventory.getPowerUp(PowerUp.Type.TIMER)!!.expirationDate.`should equal`(
                LocalDate.now().plusDays(defaultDuration.toLong())
            )
        }

        it("should extend existing PowerUp for 30 days") {
            val powerUpPrice = PowerUp.Type.TIMER.coinPrice
            val p = (executeUseCase(
                PowerUp.Type.TIMER,
                durationDays = defaultDuration,
                playerCoins = powerUpPrice
            ) as BuyPowerUpUseCase.Result.Bought).player

            val res = BuyPowerUpUseCase(
                TestUtil.playerRepoMock(
                    player = p.copy(coins = powerUpPrice)
                )
            ).execute(
                BuyPowerUpUseCase.Params(
                    PowerUp.Type.TIMER,
                    defaultDuration
                )
            ) as BuyPowerUpUseCase.Result.Bought

            res.player.inventory.getPowerUp(PowerUp.Type.TIMER)!!.expirationDate.`should equal`(
                LocalDate.now().plusDays((defaultDuration * 2).toLong())
            )
        }
    }
})