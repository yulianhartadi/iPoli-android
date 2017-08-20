package io.ipoli.android.player

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.common.navigation.Navigator
import io.ipoli.android.player.ui.SignInController
import io.ipoli.android.player.ui.SignInInitialState
import io.ipoli.android.player.ui.SignInStatePartialChange
import io.ipoli.android.player.ui.SignInViewState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/8/17.
 */
class SignInPresenter @Inject constructor(private val signInUseCase: SignInUseCase, private val navigator: Navigator) :
    MviBasePresenter<SignInController, SignInViewState>() {

    override fun bindIntents() {
        val observables = listOf<Observable<SignInStatePartialChange>>(
            intent { it.signInWithGoogleIntent() }.switchMap { signInRequest ->
                signInUseCase.execute(signInRequest)
            },
            intent { it.signInWithFacebookIntent() }.switchMap { signInRequest ->
                signInUseCase.execute(signInRequest)
            },
            intent { it.signInAsGuestIntent() }.switchMap { signInRequest ->
                signInUseCase.execute(signInRequest).doFinally {
                    navigator.showHome()
                }
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