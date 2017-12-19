package mypoli.android.player.usecase

import mypoli.android.R.id.currentPet
import mypoli.android.R.string.themes
import mypoli.android.TestUtil
import mypoli.android.pet.Pet
import mypoli.android.pet.PetAvatar
import mypoli.android.player.Inventory
import mypoli.android.player.InventoryPet
import mypoli.android.player.Player
import mypoli.android.player.Theme
import mypoli.android.theme.usecase.ChangeThemeUseCase
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/12/17.
 */
class ChangeThemeUseCaseSpek : Spek({

    describe("ChangeThemeUseCaseSpek") {

        fun executeUseCase(player: Player, theme: Theme) =
            ChangeThemeUseCase(TestUtil.playerRepoMock(player)).execute(theme)


        it("should require theme in inventory") {
            val player = TestUtil.player().copy(
                inventory = Inventory(
                    themes = setOf(Theme.RED)
                )
            )
            val exec = { executeUseCase(player, Theme.BLUE) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should change theme") {
            val currentTheme = Theme.RED
            val newTheme = Theme.BLUE
            val player = TestUtil.player().copy(
                currentTheme = currentTheme,
                inventory = Inventory(
                    themes = setOf(currentTheme, newTheme)
                )
            )

            val newPlayer = executeUseCase(player, Theme.BLUE)
            newPlayer.currentTheme.`should be`(newTheme)
        }
    }
})