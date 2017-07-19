package io.ipoli.android.rewards

import io.ipoli.android.*


/**
 * Created by vini on 7/8/17.
 */
interface RewardStatePartialChange {
    fun computeNewState(prevStateReward: RewardViewState): RewardViewState
}

class RewardsLoadingPartialChange : RewardStatePartialChange {
    override fun computeNewState(prevStateReward: RewardViewState): RewardViewState {
        return RewardsInitialLoadingState()
    }
}

class RewardsLoadedPartialChange(val data: List<Reward>) : RewardStatePartialChange {

    override fun computeNewState(prevStateReward: RewardViewState): RewardViewState {
        return RewardsLoadedState(data)
    }
}

class RewardUsedPartialChange : RewardStatePartialChange {

    override fun computeNewState(prevStateReward: RewardViewState): RewardViewState {
        return RewardUsedState(prevStateReward.rewards!!)
    }

}

class RewardDeletedPartialChange : RewardStatePartialChange {

    override fun computeNewState(prevStateReward: RewardViewState): RewardViewState {
        return RewardDeleteState(prevStateReward.rewards!!)
    }
}