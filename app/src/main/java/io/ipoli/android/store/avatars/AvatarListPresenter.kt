package io.ipoli.android.store.avatars

import android.util.Log
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.store.StoreController
import io.ipoli.android.store.StoreLoadingState
import io.ipoli.android.store.StoreStatePartialChange
import io.ipoli.android.store.StoreViewState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/20/17.
 */
class AvatarListPresenter @Inject constructor(private val displayAvatarListUseCase: DisplayAvatarListUseCase,
                                              private val buyAvatarUseCase: BuyAvatarUseCase,
                                              private val useAvatarUseCase: UseAvatarUseCase) :
    MviBasePresenter<AvatarListController, AvatarListViewState>() {
    override fun bindIntents() {
        val observables = listOf<Observable<AvatarListPartialStateChange>>(
            intent { it.displayAvatarListIntent() }.switchMap {
                displayAvatarListUseCase.execute(Unit)
            },

            intent { it.buyAvatarIntent().switchMap { avatarViewModel ->
                    buyAvatarUseCase.execute(avatarViewModel)
                }
            },

            intent { it.useAvatarIntent().switchMap { avatarViewModel ->
                    useAvatarUseCase.execute(avatarViewModel)
                }
            }
        )

        val allIntents: Observable<AvatarListPartialStateChange> = Observable.merge(observables)
        val initialState: AvatarListViewState = AvatarListLoadingViewState()

        val stateObservable = allIntents.scan(initialState, this::viewStateReducer)
            .observeOn(AndroidSchedulers.mainThread())

        subscribeViewState(stateObservable, AvatarListController::render)
    }

    private fun viewStateReducer(previousState: AvatarListViewState, statePartialChange: AvatarListPartialStateChange): AvatarListViewState {
        return statePartialChange.computeNewState(previousState)
    }
}

