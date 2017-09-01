package io.ipoli.android.reward.list

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.reward.list.usecase.DisplayRewardListUseCase
import io.ipoli.android.reward.list.usecase.RemoveRewardFromListUseCase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject


/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 7/7/17.
 */
class RewardListPresenter @Inject constructor(private val displayRewardListUseCase: DisplayRewardListUseCase,
                                              private val removeRewardFromListUseCase: RemoveRewardFromListUseCase) : MviBasePresenter<RewardListController, RewardListViewState>() {

    override fun bindIntents() {

//        val loadDataState: Observable<RewardListViewState> = intent { it.loadRewardsIntent() }
//                .switchMap { interactor.loadRewards() }
//                .switchMap { data -> Observable.just(RewardsLoadedState(data) as RewardListViewState) }
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

//        val rewardRepository = RewardRepository()
//        val r = Reward(name = "Welcome", description = "Hello sir!", price = 123)
//        rewardRepository.save(r)

        val observables = ArrayList<Observable<RewardListPartialChange>>()

//        val displayRewardListUseCase = DisplayRewardListUseCase(rewardRepository)

//        observables.add(
//                intent { it.loadRewardsIntent() }.switchMap { _ ->
//                    displayRewardListUseCase.execute(Unit)
////                            .doOnNext { rewards ->
////                                Log.d("Loading rewards", "Loading all " + rewards.size + " " + Looper.getMainLooper().isCurrentThread
////                                )
////                            }
////                            .subscribeOn(Schedulers.io())
//                })

        observables.add(
            intent { it.loadRewardsIntent() }.switchMap {
                displayRewardListUseCase.execute(Unit)
            })

        observables += intent { it.removeRewardIntent() }
            .switchMap { parameters ->
                removeRewardFromListUseCase.execute(parameters)
            }

        observables += intent { it.undoRemoveRewardIntent() }
            .switchMap { parameters ->
                removeRewardFromListUseCase.undo(parameters)
            }

//        observables.add(
//                intent { it.useRewardIntent() }
//                        .switchMap { reward ->
//                            interactor.useReward(reward)
//                                    .doOnNext { r -> Timber.d("RewardUsed", "Reward used " + r.name) }
//                                    .map { ignored -> RewardUsedPartialChange() as RewardListPartialChange }
//                                    .subscribeOn(Schedulers.io())
//                        }
//        )
//
//        observables.add(
//                intent { it.removeRewardIntent() }
//                        .doOnNext { reward -> Log.d("Deleting reward", reward.name) }
//                        .switchMap { reward -> interactor.deleteReward(reward) }
//                        .map { ignored -> RewardDeletedPartialChange() })

        val allIntents: Observable<RewardListPartialChange> = Observable.merge(observables)
        val initialState = RewardListViewState()
        val stateObservable = allIntents.scan(initialState, this::viewStateReducer)
            .observeOn(AndroidSchedulers.mainThread())

        subscribeViewState(stateObservable, RewardListController::render)
    }

    private fun viewStateReducer(prevState: RewardListViewState, partialChange: RewardListPartialChange): RewardListViewState {
        return partialChange.computeNewState(prevState)
    }
}