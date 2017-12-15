package io.ipoli.android.player.usecase

import io.ipoli.android.TestUtil
import io.ipoli.android.player.Inventory
import io.ipoli.android.player.Player
import io.ipoli.android.quest.IconPack
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 15.12.17.
 */
class BuyCoinPackUseCaseSpek : Spek({

    describe("BuyCoinPackUseCase") {
        fun executeUseCase(player: Player, iconPack: IconPack) =
            BuyCoinPackUseCase(TestUtil.playerRepoMock(player)).execute(BuyCoinPackUseCase.Params(iconPack))

        it("should require not bought icon pack") {

            val p = TestUtil.player().copy(
                inventory = Inventory(
                    iconPacks = setOf(IconPack.PACK_BASE)
                )
            )

            val exec = { executeUseCase(p, IconPack.PACK_BASE) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should not buy when not enough coins") {
            val p = TestUtil.player().copy(
                coins = IconPack.PACK_BASE.price - 1,
                inventory = Inventory()
            )
            val result = executeUseCase(p, IconPack.PACK_BASE)
            result.`should be`(BuyCoinPackUseCase.Result.TooExpensive)
        }

        it("should buy icon pack") {
            val p = TestUtil.player().copy(
                coins = IconPack.PACK_BASE.price,
                inventory = Inventory()
            )
            val result = executeUseCase(p, IconPack.PACK_BASE)
            result.`should be instance of`(BuyCoinPackUseCase.Result.IconPackBought::class)
            val newPlayer = (result as BuyCoinPackUseCase.Result.IconPackBought).player
            newPlayer.coins.`should be equal to`(p.coins - IconPack.PACK_BASE.price)
            newPlayer.hasIconPack(IconPack.PACK_BASE).`should be true`()
        }
    }

})