package io.ipoli.android.store

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/18/17.
 */
class StorePresenter :
        MviBasePresenter<StoreController, StoreViewState>() {

    override fun bindIntents() {
    }
}

