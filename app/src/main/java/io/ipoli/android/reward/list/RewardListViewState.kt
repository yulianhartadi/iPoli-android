package io.ipoli.android.reward.list

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/7/17.
 */
data class RewardListViewState(
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val isEmpty: Boolean = false,
    val hasFreshData: Boolean = false,
    val rewards: List<RewardViewModel> = listOf(),
    val isRewardRemoved: Boolean = false
)