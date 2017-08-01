package io.ipoli.android

import io.ipoli.android.reward.Reward

/**
 * Created by vini on 7/7/17.
 */
open class RewardViewState(
        val rewards: List<Reward>?
)

class RewardsLoadedState(rewards: List<Reward>) : RewardViewState(rewards)

class RewardsInitialLoadingState : RewardViewState(null)

class RewardInitialLoadingErrorState : RewardViewState(null)

class RewardUsedState(rewards: List<Reward>) : RewardViewState(rewards)

class RewardDeleteState(rewards: List<Reward>) : RewardViewState(rewards)