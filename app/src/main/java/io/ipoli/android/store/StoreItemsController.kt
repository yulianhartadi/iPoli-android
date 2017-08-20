package io.ipoli.android.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.daggerComponent
import io.ipoli.android.navigator
import io.reactivex.Observable
import kotlinx.android.synthetic.main.controller_store_items.view.*
import timber.log.Timber

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/20/17.
 */
class StoreItemsController : BaseController<StoreItemsController, StoreItemsPresenter>() {

    private var restoringState: Boolean = false

    val storeItemsComponent: StoreItemsComponent by lazy {
        val component = DaggerStoreItemsComponent
            .builder()
            .controllerComponent(daggerComponent)
            .build()
        component.inject(this@StoreItemsController)
        component
    }

    override fun createPresenter(): StoreItemsPresenter = storeItemsComponent.createStoreItemsPresenter()

    override fun setRestoringViewState(restoringViewState: Boolean) {
        this.restoringState = restoringViewState
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        storeItemsComponent // will ensure that dagger component will be initialized lazily.
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_store_items, container, false)
        view.subscriptionContainer.setOnClickListener({ navigator.showRewardsList() })
        view.powerUpsContainer.setOnClickListener({ navigator.showRewardsList() })
        view.avatarsContainer.setOnClickListener({ navigator.showRewardsList() })
        view.petsContainer.setOnClickListener({ navigator.showRewardsList() })
        return view
    }

    fun showCoinsIntent(): Observable<Boolean> {
        return Observable.just(!restoringState).filter { _ -> true }.doOnComplete { Timber.d("Coins") }
    }

    fun render(state: StoreViewState) {
        when (state) {
            is StoreLoadingState -> {
                Toast.makeText(activity, "Loading", Toast.LENGTH_SHORT).show()
            }

            is StoreLoadedState -> {
                Toast.makeText(activity, "Loaded Coins: " + state.coins, Toast.LENGTH_SHORT).show()
//                view?.playerCoins?.text = state.coins.toString()
            }
        }
    }
}