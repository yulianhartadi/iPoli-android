package io.ipoli.android.rewards

import io.reactivex.Observable
import io.realm.RealmResults


/**
 * Created by vini on 7/9/17.
 */
class RewardListInteractor {

    fun useReward(reward: Reward): Observable<Unit> {
        return Observable.just(Unit)
    }

    fun loadRewards(): Observable<RealmResults<Reward>> {
        val rewardRepository = RewardRepository()
        return rewardRepository.loadRewards()
    }

    fun deleteReward(reward: Reward): Observable<Unit> {
        val rewardRepository = RewardRepository()
        rewardRepository.delete(reward)
        return Observable.just(Unit)
    }
}