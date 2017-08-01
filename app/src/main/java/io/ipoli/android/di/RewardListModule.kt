package io.ipoli.android.di

import dagger.Module
import dagger.Provides
import io.ipoli.android.rewards.RewardRepository

/**
 * Created by vini on 8/2/17.
 */
@Module
class RewardListModule {

    @Provides
    @RewardListScope
    fun provideRewardsRepository(): RewardRepository = RewardRepository()
}