package io.ipoli.android.reward

import android.os.Looper
import android.util.Log
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.RewardViewState
import io.ipoli.android.RewardsInitialLoadingState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject


/**
 * Created by vini on 7/7/17.
 */
class RewardListPresenter @Inject constructor(private val displayRewardsUseCase: DisplayRewardsUseCase) : MviBasePresenter<RewardListController, RewardViewState>() {

    override fun bindIntents() {

//        val loadDataState: Observable<RewardViewState> = intent { it.loadRewardsIntent() }
//                .switchMap { interactor.loadRewards() }
//                .switchMap { data -> Observable.just(RewardsLoadedState(data) as RewardViewState) }
//                .startWith(RewardsInitialLoadingState())
//                .onErrorReturn { RewardInitialLoadingErrorState() }
//                .observeOn(AndroidSchedulers.mainThread())
//
//        intent { it.useRewardIntent() }
//                .doOnNext { ignored -> Log.d("Log1", "Log2 " + ignored.name + " " + ignored.id) }
//                .switchMap { reward -> interactor.useReward(reward) }
//                .subscribe()
//
//        subscribeViewState(loadDataState, RewardListController::render)

//        val r = Reward(name = "Welcome", description = "Hello sir!", price = 123)
//        rewardRepository.save(r)

        val observables = ArrayList<Observable<RewardStatePartialChange>>()

//        val displayRewardsUseCase = DisplayRewardsUseCase(rewardRepository)

        observables.add(
                intent { it.loadRewardsIntent() }.switchMap { _ ->
                    displayRewardsUseCase.execute(Unit)
//                            .doOnNext { rewards ->
//                                Log.d("Loading rewards", "Loading all " + rewards.size + " " + Looper.getMainLooper().isCurrentThread
//                                )
//                            }
//                            .subscribeOn(Schedulers.io())
                })

//        observables.add(
//                intent { it.useRewardIntent() }
//                        .switchMap { reward ->
//                            interactor.useReward(reward)
//                                    .doOnNext { r -> Timber.d("RewardUsed", "Reward used " + r.name) }
//                                    .map { ignored -> RewardUsedPartialChange() as RewardStatePartialChange }
//                                    .subscribeOn(Schedulers.io())
//                        }
//        )
//
//        observables.add(
//                intent { it.deleteRewardIntent() }
//                        .doOnNext { reward -> Log.d("Deleting reward", reward.name) }
//                        .switchMap { reward -> interactor.deleteReward(reward) }
//                        .map { ignored -> RewardDeletedPartialChange() })

        val allIntents: Observable <RewardStatePartialChange> = Observable.merge(observables)
        val initialState: RewardViewState = RewardsInitialLoadingState()
        val stateObservable = allIntents.scan(initialState, this::viewStateReducer)
                .observeOn(AndroidSchedulers.mainThread())

        subscribeViewState(stateObservable, RewardListController::render)
    }

    private fun viewStateReducer(previousStateReward: RewardViewState, statePartialChange: RewardStatePartialChange): RewardViewState {
        return statePartialChange.computeNewState(previousStateReward)
    }
}