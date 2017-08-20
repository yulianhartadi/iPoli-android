package io.ipoli.android.store

import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bluelinelabs.conductor.RouterTransaction
import io.ipoli.android.MainActivity
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.daggerComponent
import io.ipoli.android.navigator
import io.reactivex.Observable
import kotlinx.android.synthetic.main.controller_store.view.*
import timber.log.Timber

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/18/17.
 */
class StoreController : BaseController<StoreController, StorePresenter>() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return inflater.inflate(R.layout.controller_store, container, false)
    }

    override fun createPresenter(): StorePresenter = StorePresenter()

    override fun onAttach(view: View) {
        super.onAttach(view)

        val activity = activity as MainActivity
        activity.setSupportActionBar(view.toolbar)
        val actionBar = activity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val childRouter = getChildRouter(view.controllerContainer, null)
        childRouter.setRoot(RouterTransaction.with(StoreItemsController()))
    }

    override fun setRestoringViewState(restoringViewState: Boolean) {
    }


}

