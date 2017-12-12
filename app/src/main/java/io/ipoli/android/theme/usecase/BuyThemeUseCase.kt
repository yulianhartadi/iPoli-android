package io.ipoli.android.theme.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.player.Player
import io.ipoli.android.player.Theme
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.theme.usecase.BuyThemeUseCase.Result.ThemeBought
import io.ipoli.android.theme.usecase.BuyThemeUseCase.Result.TooExpensive

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/12/17.
 */
class BuyThemeUseCase(private val playerRepository: PlayerRepository) : UseCase<Theme, BuyThemeUseCase.Result> {
    override fun execute(parameters: Theme): Result {
        val theme = parameters
        val player = playerRepository.find()
        requireNotNull(player)
        require(!player!!.hasTheme(theme))

        if (player.coins < theme.price) {
            return TooExpensive
        }

        val newPlayer = player.copy(
            coins = player.coins - theme.price,
            inventory = player.inventory.addTheme(theme)
        )

        return ThemeBought(playerRepository.save(newPlayer))
    }

    sealed class Result {
        data class ThemeBought(val player: Player) : Result()
        object TooExpensive : Result()
    }
}