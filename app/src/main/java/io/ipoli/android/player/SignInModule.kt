package io.ipoli.android.player

import dagger.Module
import dagger.Provides

/**
 * Created by Venelin Valkov <venelin@curiousily.com> on 8/8/17.
 */
@Module
class SignInModule {

    @Provides
    @SignInScope
    fun provideSignInUseCase(): SignInUseCase = SignInUseCase(null, null)
}