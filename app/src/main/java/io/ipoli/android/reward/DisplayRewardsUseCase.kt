package io.ipoli.android.reward

import io.ipoli.android.SimpleRxUseCase
import io.reactivex.Observable

/**
 * Created by vini on 8/1/17.
 */
class DisplayRewardsUseCase(val rewardRepository: RewardRepository) : SimpleRxUseCase<List<Reward>>() {

    override fun createObservable(params: Unit): Observable<List<Reward>> {
        return rewardRepository.loadRewards()
    }

}