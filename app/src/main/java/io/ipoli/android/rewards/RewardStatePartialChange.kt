package io.ipoli.android.rewards

import io.ipoli.android.RewardViewState
import io.ipoli.android.RewardsLoadedState
import io.ipoli.android.RewardsLoadingState
import io.realm.RealmResults


/**
 * Created by vini on 7/8/17.
 */
interface RewardStatePartialChange {
    fun computeNewState(prevStateReward: RewardViewState): RewardViewState
}

class RewardsLoadingPartialChange : RewardStatePartialChange {
    override fun computeNewState(prevStateReward: RewardViewState): RewardViewState {
        return RewardsLoadingState()
    }
}

class RewardsLoadedPartialChange(val data: RealmResults<Reward>) : RewardStatePartialChange {

    override fun computeNewState(prevStateReward: RewardViewState): RewardViewState {
        return RewardsLoadedState(data)
    }

}