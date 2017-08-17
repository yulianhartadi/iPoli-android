package io.ipoli.android.player

import dagger.Module
import dagger.Provides

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/8/17.
 */
@Module
class SignInModule {

    @Provides
    @SignInScope
    fun providePlayerRepository(): PlayerRepository = PlayerRepository()

    @Provides
    @SignInScope
    fun provideSignInUseCase(playerRepository: PlayerRepository): SignInUseCase = SignInUseCase(playerRepository, null, null)
}