package io.ipoli.android.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.mosby3.RestoreViewOnCreateMviController
import io.ipoli.android.R
import io.ipoli.android.daggerComponent
import io.ipoli.android.player.DaggerSignInComponent

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/18/17.
 */
class StoreController : RestoreViewOnCreateMviController<StoreController, StorePresenter>() {

    val storeComponent : StoreComponent by lazy {
        val component = DaggerStoreComponent
                .builder()
                .controllerComponent(daggerComponent)
                .build()
        component.inject(this@StoreController)
        component
    }

    override fun createPresenter(): StorePresenter = storeComponent.createStorePresenter()

    override fun setRestoringViewState(restoringViewState: Boolean) {}

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        storeComponent // will ensure that dagger component will be initialized lazily.
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return inflater.inflate(R.layout.controller_store, container, false)
    }

}

