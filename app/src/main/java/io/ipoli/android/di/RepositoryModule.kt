package io.ipoli.android.di

import dagger.Module
import dagger.Provides
import io.ipoli.android.rewards.RewardRepository
import javax.inject.Singleton

/**
 * Created by vini on 8/1/17.
 */
@Module
class RepositoryModule {

    @Provides
    @Singleton
    fun provideRewardsRepository(): RewardRepository = RewardRepository()
}