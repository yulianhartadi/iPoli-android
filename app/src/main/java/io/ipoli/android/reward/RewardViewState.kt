package io.ipoli.android.reward

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/7/17.
 */
data class RewardViewState(
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val hasFreshData: Boolean = false,
    val rewards: List<RewardViewModel> = listOf()
)