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

    class Empty(private val removedReward: RewardViewModel? = null, private val removedRewardIndex: Int? = null) : RewardListPartialChange {
        override fun computeNewState(prevState: RewardListViewState): RewardListViewState =
            RewardListViewState(isEmpty = true, rewards = prevState.rewards,
                removedReward = removedReward, removedRewardIndex = removedRewardIndex,
                isRewardRemoved = removedReward != null)
    }

    class Error : RewardListPartialChange {
        override fun computeNewState(prevState: RewardListViewState): RewardListViewState =
            RewardListViewState(hasError = true, rewards = prevState.rewards)

    }

    class DataLoaded(private val rewardViews: List<RewardViewModel>) : RewardListPartialChange {
        override fun computeNewState(prevState: RewardListViewState): RewardListViewState =
            RewardListViewState(shouldShowData = true,
                rewards = rewardViews)

    }

    class RewardRemoved(private val rewardViews: List<RewardViewModel>,
                        private val removedReward: RewardViewModel,
                        private val removedRewardIndex: Int) : RewardListPartialChange {

        override fun computeNewState(prevState: RewardListViewState): RewardListViewState =
            RewardListViewState(rewards = rewardViews,
                removedReward = removedReward,
                removedRewardIndex = removedRewardIndex,
                isRewardRemoved = true,
                shouldShowData = true)
    }

    class UndoRemovedReward(private val rewardViews: List<RewardViewModel>) : RewardListPartialChange {
        override fun computeNewState(prevState: RewardListViewState): RewardListViewState =
            RewardListViewState(rewards = rewardViews, shouldShowData = true)
    }
}