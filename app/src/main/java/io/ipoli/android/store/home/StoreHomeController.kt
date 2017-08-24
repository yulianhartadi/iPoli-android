package io.ipoli.android.store.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.common.navigator
import kotlinx.android.synthetic.main.controller_store_home.view.*

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/20/17.
 */
class StoreHomeController : BaseController<StoreHomeController, StoreHomePresenter>() {

    override fun createPresenter(): StoreHomePresenter = StoreHomePresenter()

    override fun setRestoringViewState(restoringViewState: Boolean) {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_store_home, container, false)
        view.subscriptionContainer.setOnClickListener({ navigator.showRewardsList() })
        view.powerUpsContainer.setOnClickListener({ navigator.showRewardsList() })
        view.avatarsContainer.setOnClickListener({ navigator.showAvatarList() })
        view.petsContainer.setOnClickListener({ navigator.showRewardsList() })
        return view
    }
}