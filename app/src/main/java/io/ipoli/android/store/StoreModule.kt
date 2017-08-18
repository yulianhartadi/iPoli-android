package io.ipoli.android.store

import dagger.Module
import dagger.Provides
import io.ipoli.android.player.PlayerRepository
import io.ipoli.android.player.SignInScope
import io.ipoli.android.player.SignInUseCase

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/18/17.
 */
@Module
class StoreModule {
    @Provides
    @StoreScope
    fun providePlayerRepository(): PlayerRepository = PlayerRepository()

    @Provides
    @StoreScope
    fun provideDisplayCoinsUseCase(playerRepository: PlayerRepository): DisplayCoinsUseCase = DisplayCoinsUseCase(playerRepository)
}