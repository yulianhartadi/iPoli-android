package io.ipoli.android.reward.list


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/8/17.
 */
interface RewardListPartialChange {
    fun computeNewState(prevState: RewardViewState): RewardViewState

    class Loading : RewardListPartialChange {
        override fun computeNewState(prevState: RewardViewState): RewardViewState =
            RewardViewState(isLoading = true, rewards = prevState.rewards)
    }

    class Empty : RewardListPartialChange {
        override fun computeNewState(prevState: RewardViewState): RewardViewState =
            RewardViewState(isEmpty = true, rewards = prevState.rewards)
    }

    class Error : RewardListPartialChange {
        override fun computeNewState(prevState: RewardViewState): RewardViewState =
            RewardViewState(hasError = true, rewards = prevState.rewards)

    }

    class DataLoaded(val rewardViews: List<RewardViewModel>) : RewardListPartialChange {
        override fun computeNewState(prevState: RewardViewState): RewardViewState =
            RewardViewState(hasFreshData = true,
                rewards = rewardViews,
                shouldShowData = true)

    }
}