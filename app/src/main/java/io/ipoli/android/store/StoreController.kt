package io.ipoli.android.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bluelinelabs.conductor.RouterTransaction
import io.ipoli.android.MainActivity
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.daggerComponent
import io.ipoli.android.store.home.StoreHomeController
import io.reactivex.Observable
import kotlinx.android.synthetic.main.controller_store.view.*
import timber.log.Timber

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/18/17.
 */
class StoreController : BaseController<StoreController, StorePresenter>() {
    private var restoringState: Boolean = false

    val storeComponent: StoreComponent by lazy {
        val component = DaggerStoreComponent.builder()
            .controllerComponent(daggerComponent)
            .build()
        component.inject(this@StoreController)
        component
    }

    override fun createPresenter(): StorePresenter = storeComponent.createStorePresenter()

    override fun setRestoringViewState(restoringViewState: Boolean) {
        this.restoringState = restoringViewState
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        storeComponent // will ensure that dagger component will be initialized lazily.
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return inflater.inflate(R.layout.controller_store, container, false)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        val activity = activity as MainActivity
        activity.setSupportActionBar(view.toolbar)
        val actionBar = activity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val childRouter = getChildRouter(view.controllerContainer, null)
        childRouter.setRoot(RouterTransaction.with(StoreHomeController()))
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
                view?.playerCoins?.text = state.coins.toString()
            }
        }
    }

}

