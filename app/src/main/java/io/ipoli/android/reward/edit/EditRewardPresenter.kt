package io.ipoli.android.reward.edit

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.reward.list.DisplayRewardsUseCase
import io.ipoli.android.reward.list.RewardViewState
import javax.inject.Inject

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/7/17.
 */
class EditRewardPresenter @Inject constructor() : MviBasePresenter<EditRewardController, RewardViewState>() {
    override fun bindIntents() {

    }
}