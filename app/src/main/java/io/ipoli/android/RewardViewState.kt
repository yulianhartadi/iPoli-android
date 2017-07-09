package io.ipoli.android

import io.ipoli.android.rewards.Reward
import io.realm.RealmResults

/**
 * Created by vini on 7/7/17.
 */
open class RewardViewState(
        val rewards: RealmResults<Reward>?
)

class RewardsLoadedState(rewards: RealmResults<Reward>?) : RewardViewState(rewards)

class RewardsLoadingState : RewardViewState(null)

class RewardLoadingErrorState : RewardViewState(null)