package io.ipoli.android.rewards

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.RewardViewState
import io.ipoli.android.RewardsLoadingState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**
 * Created by vini on 7/7/17.
 */
class RewardsPresenter(private val interactor: RewardListInteractor) : MviBasePresenter<RewardsController, RewardViewState>() {

    override fun bindIntents() {

//        val loadDataState: Observable<RewardViewState> = intent { it.loadRewardsIntent() }
//                .switchMap { interactor.loadRewards() }
//                .switchMap { data -> Observable.just(RewardsLoadedState(data) as RewardViewState) }
//                .startWith(RewardsLoadingState())
//                .onErrorReturn { RewardLoadingErrorState() }
//                .observeOn(AndroidSchedulers.mainThread())
//
//        intent { it.useRewardIntent() }
//                .doOnNext { ignored -> Log.d("Log1", "Log2 " + ignored.name + " " + ignored.id) }
//                .switchMap { reward -> interactor.useReward(reward) }
//                .subscribe()
//
//        subscribeViewState(loadDataState, RewardsController::render)

        val observables = ArrayList<Observable<RewardStatePartialChange>>()

//
        observables.add(intent { view -> view.loadRewardsIntent() }.flatMap { ignored ->
            interactor.loadRewards()
                    .map { data -> RewardsLoadedPartialChange(data) as RewardStatePartialChange }
                    .startWith(RewardsLoadingPartialChange()).subscribeOn(Schedulers.io())
        })

        val allIntents: Observable<RewardStatePartialChange> = Observable.merge(observables)
        val initialState: RewardViewState = RewardsLoadingState()
        val stateObservable = allIntents.scan(initialState, this::viewStateReducer)
                .observeOn(AndroidSchedulers.mainThread())

        subscribeViewState(stateObservable, RewardsController::render)

//        val stateObservable = allIntents.scan(initialState, {t1, t2 -> viewStateReducer(t2 as RewardViewState, t1) })
//                .observeOn(AndroidSchedulers.mainThread())
//
//        subscribeViewState(stateObservable, {view -> RewardsController::loadRewardsIntent})
    }

    private fun viewStateReducer(previousStateReward: RewardViewState, statePartialChange: RewardStatePartialChange): RewardViewState {
        return statePartialChange.computeNewState(previousStateReward)
    }
}