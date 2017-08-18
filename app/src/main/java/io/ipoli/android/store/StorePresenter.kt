package io.ipoli.android.store

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.navigation.Navigator
import javax.inject.Inject

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/18/17.
 */
class StorePresenter @Inject constructor(private val navigator: Navigator) :
        MviBasePresenter<StoreController, StoreViewState>() {

    override fun bindIntents() {

    }

}

