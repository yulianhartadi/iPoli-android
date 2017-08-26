package io.ipoli.android.reward


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/8/17.
 */
interface RewardListPartialChange {
    fun computeNewState(prevState: RewardViewState): RewardViewState

    class Loading : RewardListPartialChange {
        override fun computeNewState(prevState: RewardViewState): RewardViewState =
            RewardViewState(isLoading = true)
    }

    class Empty : RewardListPartialChange {
        override fun computeNewState(prevState: RewardViewState): RewardViewState =
            RewardViewState()
    }

    class Error : RewardListPartialChange {
        override fun computeNewState(prevState: RewardViewState): RewardViewState =
            RewardViewState(hasError = true, rewards = prevState.rewards)

    }

    class DataLoaded(val rewardViews: List<RewardViewModel>) : RewardListPartialChange {
        override fun computeNewState(prevState: RewardViewState): RewardViewState =
            RewardViewState(hasFreshData = true, rewards = rewardViews)

    }
}