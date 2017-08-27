package io.ipoli.android.reward.list


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/8/17.
 */
interface RewardListPartialChange {
    fun computeNewState(prevState: RewardListViewState): RewardListViewState

    class Loading : RewardListPartialChange {
        override fun computeNewState(prevState: RewardListViewState): RewardListViewState =
            RewardListViewState(isLoading = true, rewards = prevState.rewards)
    }

    class Empty : RewardListPartialChange {
        override fun computeNewState(prevState: RewardListViewState): RewardListViewState =
            RewardListViewState(isEmpty = true, rewards = prevState.rewards)
    }

    class Error : RewardListPartialChange {
        override fun computeNewState(prevState: RewardListViewState): RewardListViewState =
            RewardListViewState(hasError = true, rewards = prevState.rewards)

    }

    class DataLoaded(val rewardViews: List<RewardViewModel>) : RewardListPartialChange {
        override fun computeNewState(prevState: RewardListViewState): RewardListViewState =
            RewardListViewState(hasFreshData = true,
                rewards = rewardViews)

    }

    class RewardRemoved(val rewardViews: List<RewardViewModel>) : RewardListPartialChange {

        override fun computeNewState(prevState: RewardListViewState): RewardListViewState =
            RewardListViewState(rewards = rewardViews,
                isRewardRemoved = true,
                hasFreshData = true)
    }
}