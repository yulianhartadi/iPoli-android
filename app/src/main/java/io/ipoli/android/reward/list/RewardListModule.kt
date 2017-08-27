package io.ipoli.android.reward.list

import dagger.Module
import dagger.Provides
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.persistence.RealmPlayerRepository
import io.ipoli.android.reward.RealmRewardRepository
import io.ipoli.android.reward.RewardRepository

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/2/17.
 */
@Module
class RewardListModule {

    @Provides
    @RewardListScope
    fun provideRewardsRepository(): RewardRepository = RealmRewardRepository()

    @Provides
    @RewardListScope
    fun providePlayerRepository(): PlayerRepository = RealmPlayerRepository()

    @Provides
    @RewardListScope
    fun provideRewardListUseCase(rewardRepository: RewardRepository, playerRepository: PlayerRepository): DisplayRewardsUseCase {
        return DisplayRewardsUseCase(rewardRepository, playerRepository)
    }
}