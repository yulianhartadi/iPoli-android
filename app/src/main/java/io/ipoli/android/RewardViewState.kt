package io.ipoli.android

import io.ipoli.android.reward.Reward
import io.ipoli.android.reward.RewardModel

/**
 * Created by vini on 7/7/17.
 */
open class RewardViewState(
        val rewards: List<RewardModel>?
)

class RewardsLoadedState(rewards: List<RewardModel>) : RewardViewState(rewards)

class RewardsInitialLoadingState : RewardViewState(null)

class RewardInitialLoadingErrorState : RewardViewState(null)

class RewardUsedState(rewards: List<RewardModel>) : RewardViewState(rewards)

class RewardDeleteState(rewards: List<RewardModel>) : RewardViewState(rewards)