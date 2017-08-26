package io.ipoli.android.reward


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/8/17.
 */
interface RewardListPartialChange {
    fun computeNewState(prevState: RewardViewState): RewardViewState

    class Loading : RewardListPartialChange {
        override fun computeNewState(prevState: RewardViewState): RewardViewState =
            RewardViewState(isLoading = true,
                rewardViews = prevState.rewardViews)
    }

    class Empty : RewardListPartialChange {
        override fun computeNewState(prevState: RewardViewState): RewardViewState =
            RewardViewState(rewardViews = prevState.rewardViews)
    }

    class Error : RewardListPartialChange {
        override fun computeNewState(prevState: RewardViewState): RewardViewState =
            RewardViewState(hasError = true, rewardViews = prevState.rewardViews)

    }

    class DataLoaded(val rewardViews: List<RewardViewModel>) : RewardListPartialChange {
        override fun computeNewState(prevState: RewardViewState): RewardViewState =
            RewardViewState(hasFreshData = true, rewardViews = rewardViews)

    }
}