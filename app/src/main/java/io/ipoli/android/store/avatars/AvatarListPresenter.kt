package io.ipoli.android.store.avatars

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/20/17.
 */
class AvatarListPresenter constructor(private val displayAvatarListUseCase: DisplayAvatarListUseCase,
                                              private val buyAvatarUseCase: BuyAvatarUseCase,
                                              private val useAvatarUseCase: UseAvatarUseCase) :
    MviBasePresenter<AvatarListController, AvatarListViewState>() {

    override fun bindIntents() {
        val observables = listOf<Observable<AvatarListPartialChange>>(
            intent { it.displayAvatarListIntent() }.switchMap {
                displayAvatarListUseCase.execute(Unit)
            },

            intent { it.buyAvatarIntent() }.switchMap { avatarViewModel ->
                buyAvatarUseCase.execute(avatarViewModel)
            },

            intent { it.useAvatarIntent() }.switchMap { avatarViewModel ->
                useAvatarUseCase.execute(avatarViewModel)
            }
        )

        val allIntents: Observable<AvatarListPartialChange> = Observable.merge(observables)
        val initialState = AvatarListViewState(loading = true)

        val stateObservable = allIntents.scan(initialState, this::viewStateReducer)
            .observeOn(AndroidSchedulers.mainThread())

        subscribeViewState(stateObservable, AvatarListController::render)
    }

    private fun viewStateReducer(previousState: AvatarListViewState, statePartialChange: AvatarListPartialChange): AvatarListViewState {
        return statePartialChange.computeNewState(previousState)
    }
}

