package io.ipoli.android.reward.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.common.daggerComponent
import io.ipoli.android.reward.edit.di.DaggerEditRewardComponent
import io.ipoli.android.reward.edit.di.EditRewardComponent

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 7/7/17.
 */
class EditRewardController(val rewardId: String = "") : BaseController<EditRewardController, EditRewardPresenter, EditRewardComponent>() {
    override fun buildComponent(): EditRewardComponent =
        DaggerEditRewardComponent.builder()
            .controllerComponent(daggerComponent)
            .build()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_edit_reward, container, false) as ViewGroup
//        val rewardRepository = RealmRewardRepository()
//        val reward = rewardRepository.listenById(rewardId)
//        view.name.setText(reward.name)
//        view.description.setText(reward.description)
//        view.price.setText(reward.price.toString())
        return view
    }
}