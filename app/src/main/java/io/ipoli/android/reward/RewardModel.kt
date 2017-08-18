package io.ipoli.android.reward

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/2/17.
 */
data class RewardModel(val id: String,
                       val name: String,
                       val description: String,
                       val price: Int,
                       val canBePurchased: Boolean)