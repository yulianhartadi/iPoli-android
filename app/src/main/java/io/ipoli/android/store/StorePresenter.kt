package io.ipoli.android.store

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.RewardViewState
import io.ipoli.android.navigation.Navigator
import io.ipoli.android.player.SignInController
import io.ipoli.android.player.SignInInitialState
import io.ipoli.android.player.SignInStatePartialChange
import io.ipoli.android.player.SignInViewState
import io.ipoli.android.reward.RewardStatePartialChange
import timber.log.Timber
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/18/17.
 */
class StorePresenter @Inject constructor(private val displayCoinsUseCase: DisplayCoinsUseCase) :
        MviBasePresenter<StoreController, StoreViewState>() {

    override fun bindIntents() {
        val observables = ArrayList<Observable<StoreStatePartialChange>>()
        observables.add(
                intent { it.showCoinsIntent() }.switchMap {
                    displayCoinsUseCase.execute(Unit)
                })

        val allIntents: Observable<StoreStatePartialChange> = Observable.merge(observables)
        val initialState: StoreViewState = StoreLoadingState()

        val stateObservable = allIntents.scan(initialState, this::viewStateReducer)
            .observeOn(AndroidSchedulers.mainThread())

        subscribeViewState(stateObservable, StoreController::render)
    }

    private fun viewStateReducer(previousState: StoreViewState, statePartialChange: StoreStatePartialChange): StoreViewState {
        return statePartialChange.computeNewState(previousState)
    }
}

