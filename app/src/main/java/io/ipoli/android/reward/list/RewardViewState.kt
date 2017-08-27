package io.ipoli.android.reward.list

import io.ipoli.android.reward.list.RewardViewModel

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/7/17.
 */
data class RewardViewState(
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val isEmpty: Boolean = false,
    val hasFreshData: Boolean = false,
    val shouldShowData: Boolean = false,
    val rewards: List<RewardViewModel> = listOf()
)