package io.ipoli.android.reward

/**
 * Created by Venelin Valkov <venelin@curiousily.com> on 7/7/17.
 */
open class RewardViewState(
        val rewards: List<RewardModel>?
)

class RewardsLoadedState(rewards: List<RewardModel>) : RewardViewState(rewards)

class RewardsInitialLoadingState : RewardViewState(null)

class RewardInitialLoadingErrorState : RewardViewState(null)

class RewardUsedState(rewards: List<RewardModel>) : RewardViewState(rewards)

class RewardDeleteState(rewards: List<RewardModel>) : RewardViewState(rewards)