package io.ipoli.android.di

import dagger.Module
import dagger.Provides
import io.ipoli.android.reward.RewardRepository
import javax.inject.Singleton

/**
 * Created by Venelin Valkov <venelin@curiousily.com> on 8/1/17.
 */
@Module
class RepositoryModule {

    @Provides
    @Singleton
    fun provideRewardsRepository(): RewardRepository = RewardRepository()
}