package io.ipoli.android.rewards

import io.ipoli.android.RewardViewState
import io.realm.RealmResults


/**
 * Created by vini on 7/8/17.
 */
interface RewardStateChange {
    fun computeNewState(prevStateReward: RewardViewState): RewardViewState
}

class LoadingData : RewardStateChange {
    override fun computeNewState(prevStateReward: RewardViewState): RewardViewState {
        return RewardViewState(true, null)
    }
}

class RewardsLoaded(val data: RealmResults<Reward>) : RewardStateChange {

    override fun computeNewState(prevStateReward: RewardViewState): RewardViewState {
        return RewardViewState(false, data)
    }

}