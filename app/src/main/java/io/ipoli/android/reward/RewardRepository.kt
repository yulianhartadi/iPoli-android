package io.ipoli.android.reward

import io.ipoli.android.common.BaseRealmRepository
import io.reactivex.Observable
import io.realm.Realm
import java.util.*

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/7/17.
 */
class RewardRepository : BaseRealmRepository<Reward>() {

    override fun getModelClass(): Class<Reward> = Reward::class.java

    val realm: Realm = Realm.getDefaultInstance()

    fun loadRewards(): Observable<List<Reward>> = queryAll()

    fun findById(id: String): Reward {
        return realm.where(Reward::class.java).equalTo("id", id).findFirst()
    }

    fun save(reward: Reward) {
        realm.executeTransaction {
            val rewardObject = realm.createObject(Reward::class.java, UUID.randomUUID().toString())
            rewardObject.name = reward.name
            rewardObject.description = reward.description
            reward.id = rewardObject.id
        }
    }

    fun delete(reward: Reward) {

        val id = reward.id

        realm.executeTransactionAsync({ realmAsync ->
            val realmReward = realmAsync.where(Reward::class.java).equalTo("id", id).findFirst()
            realmReward.deleteFromRealm()
        })
    }

//
//    private fun getRealm():io.reactivex.Flowable<Realm> {
//        return io.reactivex.Flowable.create(object: FlowableOnSubscribe<Realm> {
//            @Throws(Exception::class)
//            fun subscribe(emitter:FlowableEmitter<Realm>) {
//                val realmConfiguration = realm.getConfiguration()
//                val observableRealm = Realm.getInstance(realmConfiguration)
//                val listener = { _realm-> emitter.onNext(_realm) }
//                emitter.setDisposable(Disposables.fromRunnable({ observableRealm.removeChangeListener(listener)
//                    observableRealm.close() }))
//                observableRealm.addChangeListener(listener)
//                emitter.onNext(observableRealm)
//            }
//        }, BackpressureStrategy.LATEST)
//    }

}