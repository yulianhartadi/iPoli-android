package io.ipoli.android.store

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/18/17.
 */
interface StoreStatePartialChange {
    fun computeNewState(prevState : StoreViewState) : StoreViewState
}

class StoreLoadingPartialChange : StoreStatePartialChange {
    override fun computeNewState(prevState: StoreViewState): StoreViewState {
        return StoreLoadingState()
    }
}

class StoreLoadedPartialChange(val coins: Int) : StoreStatePartialChange {
    override fun computeNewState(prevState: StoreViewState): StoreViewState {
        return StoreLoadedState(coins)
    }
}