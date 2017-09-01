package io.ipoli.android.reward.edit

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.reward.list.RewardListViewState
import javax.inject.Inject

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 7/7/17.
 */
class EditRewardPresenter @Inject constructor() : MviBasePresenter<EditRewardController, RewardListViewState>() {
    override fun bindIntents() {

    }
}