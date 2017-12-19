package mypoli.android.player.usecase

import mypoli.android.TestUtil
import mypoli.android.player.Inventory
import mypoli.android.player.Player
import mypoli.android.quest.IconPack
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 15.12.17.
 */
class BuyIconPackUseCaseSpek : Spek({

    describe("BuyIconPackUseCase") {
        fun executeUseCase(player: Player, iconPack: IconPack) =
            BuyIconPackUseCase(TestUtil.playerRepoMock(player)).execute(BuyIconPackUseCase.Params(iconPack))

        it("should require not bought icon pack") {

            val p = TestUtil.player().copy(
                inventory = Inventory(
                    iconPacks = setOf(IconPack.BASIC)
                )
            )

            val exec = { executeUseCase(p, IconPack.BASIC) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should not buy when not enough coins") {
            val p = TestUtil.player().copy(
                coins = IconPack.BASIC.price - 1,
                inventory = Inventory()
            )
            val result = executeUseCase(p, IconPack.BASIC)
            result.`should be`(BuyIconPackUseCase.Result.TooExpensive)
        }

        it("should buy icon pack") {
            val p = TestUtil.player().copy(
                coins = IconPack.BASIC.price,
                inventory = Inventory()
            )
            val result = executeUseCase(p, IconPack.BASIC)
            result.`should be instance of`(BuyIconPackUseCase.Result.IconPackBought::class)
            val newPlayer = (result as BuyIconPackUseCase.Result.IconPackBought).player
            newPlayer.coins.`should be equal to`(p.coins - IconPack.BASIC.price)
            newPlayer.hasIconPack(IconPack.BASIC).`should be true`()
        }
    }

})