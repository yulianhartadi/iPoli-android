package io.ipoli.android.rewards

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.RewardViewState
import io.ipoli.android.RewardsLoadedState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers


/**
 * Created by vini on 7/7/17.
 */
class RewardsPresenter : MviBasePresenter<RewardsController, RewardViewState>() {

    override fun bindIntents() {

        val rewardRepository = RewardRepository()
        val loadDataState: Observable<RewardViewState> = intent { it.loadData() }
                .switchMap { rewardRepository.loadRewards() }
                .switchMap { t -> Observable.just(RewardsLoadedState(t) as RewardViewState) }
                .observeOn(AndroidSchedulers.mainThread())
        subscribeViewState(loadDataState, RewardsController::render)

//        val observables = ArrayList<Observable<RewardStateChange>>()


//
//        observables.add(intent{view -> view.loadData()}.flatMap { ignored ->
//            rewardRepository.loadRewards()
//                    .map { data -> RewardsLoaded(data) as RewardStateChange }
//                    .startWith(LoadingData()).subscribeOn(Schedulers.io())
//        })

//        val allIntents: Observable<RewardStateChange> = Observable.merge(observables)
//        val initialState: RewardStateChange = LoadingData()
//        allIntents.scan(initialState, this::viewStateReducer)
//                .observeOn(AndroidSchedulers.mainThread())
//        val stateObservable = allIntents.scan(initialState, {t1, t2 -> viewStateReducer(t2 as RewardViewState, t1) })
//                .observeOn(AndroidSchedulers.mainThread())
//
//        subscribeViewState(stateObservable, {view -> RewardsController::loadData})
    }

    private fun viewStateReducer(previousStateReward: RewardViewState, stateChange: RewardStateChange): RewardViewState {
        return stateChange.computeNewState(previousStateReward)
    }
}