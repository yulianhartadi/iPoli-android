package io.ipoli.android.store.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.navigator
import kotlinx.android.synthetic.main.controller_store_items.view.*

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/20/17.
 */
class StoreItemsController : BaseController<StoreItemsController, StoreItemsPresenter>() {

    override fun createPresenter(): StoreItemsPresenter = StoreItemsPresenter()

    override fun setRestoringViewState(restoringViewState: Boolean) {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_store_items, container, false)
        view.subscriptionContainer.setOnClickListener({ navigator.showRewardsList() })
        view.powerUpsContainer.setOnClickListener({ navigator.showRewardsList() })
        view.avatarsContainer.setOnClickListener({ navigator.showAvatarList() })
        view.petsContainer.setOnClickListener({ navigator.showRewardsList() })
        return view
    }
}