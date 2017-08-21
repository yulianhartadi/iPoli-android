package io.ipoli.android.player.di

import dagger.Module
import dagger.Provides
import io.ipoli.android.player.SignInUseCase
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.persistence.RealmPlayerRepository

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/8/17.
 */
@Module
class SignInModule {

    @Provides
    @SignInScope
    fun providePlayerRepository(): PlayerRepository = RealmPlayerRepository()

    @Provides
    @SignInScope
    fun provideSignInUseCase(playerRepository: PlayerRepository): SignInUseCase = SignInUseCase(playerRepository)
}