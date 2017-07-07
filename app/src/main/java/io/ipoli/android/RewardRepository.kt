package io.ipoli.android

import io.realm.Realm
import io.realm.RealmResults
import java.util.*

/**
 * Created by vini on 7/7/17.
 */
class RewardRepository {
    val realm: Realm = Realm.getDefaultInstance()

    fun loadRewards(): RealmResults<Reward> {
        return realm.where(Reward::class.java).findAll()
    }

    fun save(reward: Reward) {
        realm.executeTransaction {
            val rewardObject = realm.createObject(Reward::class.java, UUID.randomUUID().toString())
            rewardObject.name = reward.name
            rewardObject.description = reward.description
            reward.id = rewardObject.id
        }
    }
}