package io.ipoli.android.store

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/18/17.
 */
open class StoreViewState

class StoreLoadingState : StoreViewState()

class StoreLoadedState(val coins: Int) : StoreViewState()