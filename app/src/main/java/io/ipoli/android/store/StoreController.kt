package io.ipoli.android.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import io.ipoli.android.MainActivity
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.player.persistence.RealmPlayerRepository
import io.ipoli.android.store.home.StoreHomeController
import io.reactivex.Observable
import kotlinx.android.synthetic.main.controller_store.view.*
import timber.log.Timber

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/18/17.
 */
class StoreController : BaseController<StoreController, StorePresenter>() {

//    override fun buildComponent(): StoreComponent =
//        DaggerStoreComponent.builder()
//            .controllerComponent(daggerComponent)
//            .build()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return inflater.inflate(R.layout.controller_store, container, false)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        val activity = activity as MainActivity
        activity.setSupportActionBar(view.toolbar)
        val actionBar = activity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val handler = FadeChangeHandler()
        val childRouter = getChildRouter(view.controllerContainer, null)
        childRouter.setRoot(
            RouterTransaction.with(StoreHomeController())
                .pushChangeHandler(handler)
                .popChangeHandler(handler)
        )
    }

    override fun createPresenter(): StorePresenter {
        // @TODO fix me
        return StorePresenter(DisplayCoinsUseCase(RealmPlayerRepository()))
    }

    fun showCoinsIntent(): Observable<Boolean> {
        return Observable.just(creatingState).filter { _ -> true }.doOnComplete { Timber.d("Coins") }
    }

    fun render(state: StoreViewState) {
        when (state) {
            is StoreLoadingState -> {
            }

            is StoreLoadedState -> {
                view?.playerCoins?.text = state.coins.toString()
            }
        }
    }

}

