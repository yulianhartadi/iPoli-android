package io.ipoli.android.di

import dagger.Module
import dagger.Provides
import io.ipoli.android.player.PlayerRepository
import io.ipoli.android.reward.DisplayRewardsUseCase
import io.ipoli.android.reward.RewardRepository

/**
 * Created by vini on 8/2/17.
 */
@Module
class RewardListModule {

    @Provides
    @RewardListScope
    fun provideRewardsRepository(): RewardRepository = RewardRepository()

    @Provides
    @RewardListScope
    fun providePlayerRepository(): PlayerRepository = PlayerRepository()

    @Provides
    @RewardListScope
    fun provideRewardListUseCase(rewardRepository: RewardRepository, playerRepository: PlayerRepository): DisplayRewardsUseCase {
        return DisplayRewardsUseCase(rewardRepository, playerRepository, null, null)
    }
}