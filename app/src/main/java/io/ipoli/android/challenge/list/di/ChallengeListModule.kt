package io.ipoli.android.challenge.list.di

import dagger.Module
import dagger.Provides
import io.ipoli.android.challenge.list.usecase.DisplayChallengeListUseCase
import io.ipoli.android.challenge.persistence.ChallengeRepository
import io.ipoli.android.challenge.persistence.RealmChallengeRepository

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/23/17.
 */
@Module
class ChallengeListModule {

    @Provides
    @ChallengeListScope
    fun provideChallengeRepository(): ChallengeRepository =
        RealmChallengeRepository()

    @Provides
    @ChallengeListScope
    fun provideDisplayChallengeListUseCase(challengeRepository: ChallengeRepository): DisplayChallengeListUseCase =
        DisplayChallengeListUseCase(challengeRepository)
}