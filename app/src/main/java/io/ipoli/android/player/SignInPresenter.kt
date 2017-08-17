package io.ipoli.android.player

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.RewardViewState
import io.ipoli.android.RewardsInitialLoadingState
import io.ipoli.android.reward.RewardListController
import io.ipoli.android.reward.RewardStatePartialChange
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
* Created by Venelin Valkov <venelin@curiousily.com>
* on 8/8/17.
*/
class SignInPresenter @Inject constructor(private val signInUseCase: SignInUseCase) : MviBasePresenter<SignInController, SignInViewState>() {

    override fun bindIntents() {
        val observables = ArrayList<Observable<SignInStatePartialChange>>()

        observables.add(
                intent { it.signInWithGoogleIntent() }.switchMap { signInRequest ->
                    signInUseCase.execute(signInRequest)
                })

        val allIntents: Observable<SignInStatePartialChange> = Observable.merge(observables)
        val initialState: SignInViewState = SignInInitialState()

        val stateObservable = allIntents.scan(initialState, this::viewStateReducer)
                .observeOn(AndroidSchedulers.mainThread())

        subscribeViewState(stateObservable, SignInController::render)
    }

    private fun viewStateReducer(previousState: SignInViewState, statePartialChange: SignInStatePartialChange): SignInViewState {
        return statePartialChange.computeNewState(previousState)
    }
}