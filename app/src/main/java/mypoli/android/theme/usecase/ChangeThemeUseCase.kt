package mypoli.android.theme.usecase

import mypoli.android.common.UseCase
import mypoli.android.player.Player
import mypoli.android.player.Theme
import mypoli.android.player.persistence.PlayerRepository

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 12/12/17.
 */
class ChangeThemeUseCase(private val playerRepository: PlayerRepository) : UseCase<Theme, Player> {
    override fun execute(parameters: Theme): Player {
        val theme = parameters
        val player = playerRepository.find()
        requireNotNull(player)
        require(player!!.hasTheme(theme))

        val newPlayer = player.copy(
            currentTheme = theme
        )

        return playerRepository.save(newPlayer)
    }
}