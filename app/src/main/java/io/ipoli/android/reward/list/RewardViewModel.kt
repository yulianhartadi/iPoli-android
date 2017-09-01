package io.ipoli.android.reward.list

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/2/17.
 */
data class RewardViewModel(val id: String,
                           val name: String,
                           val description: String,
                           val price: Int,
                           val canBePurchased: Boolean)