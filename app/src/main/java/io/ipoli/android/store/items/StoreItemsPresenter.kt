package io.ipoli.android.store.items

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.store.DisplayCoinsUseCase
import io.ipoli.android.store.StoreLoadingState
import io.ipoli.android.store.StoreStatePartialChange
import io.ipoli.android.store.StoreViewState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/20/17.
 */
class StoreItemsPresenter : MviBasePresenter<StoreItemsController, StoreViewState>() {

    override fun bindIntents() {
    }
}