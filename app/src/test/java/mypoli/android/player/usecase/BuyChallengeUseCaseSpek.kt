package mypoli.android.player.usecase

import mypoli.android.TestUtil
import mypoli.android.challenge.data.PredefinedChallenge
import mypoli.android.challenge.usecase.BuyChallengeUseCase
import mypoli.android.player.Inventory
import mypoli.android.player.Player
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 1/2/18.
 */
class BuyChallengeUseCaseSpek : Spek({

    describe("BuyChallengeUseCase") {

        fun executeUseCase(player: Player, challenge: PredefinedChallenge) =
            BuyChallengeUseCase(TestUtil.playerRepoMock(player)).execute(
                BuyChallengeUseCase.Params(
                    challenge
                )
            )


        it("should require not bought challenge") {
            val player = TestUtil.player().copy(
                inventory = Inventory(challenges = setOf(PredefinedChallenge.STRESS_FREE_MIND))
            )
            val exec = { executeUseCase(player, PredefinedChallenge.STRESS_FREE_MIND) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should not buy when not enough gems") {
            val player = TestUtil.player().copy(
                gems = PredefinedChallenge.STRESS_FREE_MIND.gemPrice - 1,
                inventory = Inventory()
            )
            val result = executeUseCase(player, PredefinedChallenge.STRESS_FREE_MIND)
            result.`should be`(BuyChallengeUseCase.Result.TooExpensive)
        }

        it("should buy challenge") {
            val player = TestUtil.player().copy(
                gems = PredefinedChallenge.STRESS_FREE_MIND.gemPrice,
                inventory = Inventory()
            )
            val result = executeUseCase(player, PredefinedChallenge.STRESS_FREE_MIND)
            result.`should be instance of`(BuyChallengeUseCase.Result.ChallengeBought::class)
            val newPlayer = (result as BuyChallengeUseCase.Result.ChallengeBought).player
            newPlayer.gems.`should be equal to`(player.gems - PredefinedChallenge.STRESS_FREE_MIND.gemPrice)
            newPlayer.hasChallenge(PredefinedChallenge.STRESS_FREE_MIND).`should be true`()
        }

    }
})