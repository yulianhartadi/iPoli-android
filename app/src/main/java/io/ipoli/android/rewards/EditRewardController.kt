package io.ipoli.android.rewards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.mosby3.RestoreViewOnCreateMviController
import io.ipoli.android.R
import kotlinx.android.synthetic.main.controller_edit_reward.view.*

/**
 * Created by vini on 7/7/17.
 */
class EditRewardController(val rewardId: String = "") : RestoreViewOnCreateMviController<EditRewardController, EditRewardPresenter>() {

    private var restoringState: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_edit_reward, container, false) as ViewGroup
        val rewardRepository = RewardRepository()
        val reward = rewardRepository.findById(rewardId)
        view.description.setText(reward.description)
        return view
    }

    override fun setRestoringViewState(restoringViewState: Boolean) {
        this.restoringState = restoringViewState
    }

    override fun createPresenter(): EditRewardPresenter {
        return EditRewardPresenter()
    }

}