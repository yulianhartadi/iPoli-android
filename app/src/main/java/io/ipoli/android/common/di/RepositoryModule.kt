package io.ipoli.android.common.di

import dagger.Module
import dagger.Provides
import io.ipoli.android.reward.RealmRewardRepository
import io.ipoli.android.reward.RewardRepository
import javax.inject.Singleton

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/1/17.
 */
@Module
class RepositoryModule {

    @Provides
    @Singleton
    fun provideRewardsRepository(): RewardRepository = RealmRewardRepository()
}