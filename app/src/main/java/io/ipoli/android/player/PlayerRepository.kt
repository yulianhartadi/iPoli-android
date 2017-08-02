package io.ipoli.android.player

import io.ipoli.android.reward.Reward
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposables
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import java.util.*

/**
 * Created by vini on 8/2/17.
 */
class PlayerRepository {

    val realm: Realm = Realm.getDefaultInstance()

fun getAllPlayers(): Observable<List<Player>> {
    return Observable.create { emitter ->
        val realm = Realm.getDefaultInstance()
        val players = realm.where(Player::class.java).findAll()

        val listener = RealmChangeListener<RealmResults<Player>> { players ->
            if (!emitter.isDisposed) {
                emitter.onNext(realm.copyFromRealm(players))
            }
        }

        emitter.setDisposable(Disposables.fromRunnable(Runnable {
            players.removeChangeListener(listener)
            realm.close()
        }))
        emitter.onNext(realm.copyFromRealm(players))
    }
}

    fun get(): Observable<Player> {
        return Observable.create({ emitter ->
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction { r ->
                emitter.onNext(r.copyFromRealm(r.where(Player::class.java).findFirst()))
                emitter.onComplete()
            }
            realm.close()
        })

//            val realm = Realm.getDefaultInstance()
//            val results = realm.where(Player::class.java).findFirst()
//
////                final RealmChangeListener<RealmResults<Reward>> listener = _realm -> {
////                    if (!emitter.isUnsubscribed()) {
////                        emitter.onNext(results);
////                    }
////                };
//
//            val listener = RealmChangeListener<Player> { rewards ->
//                if (!emitter.isDisposed()) {
//                    realm.executeTransaction { realm -> emitter.onNext(realm.copyFromRealm(rewards)) }
//                }
//            }
//
//            //                    @Override
//            //                    public void onChange(Realm realmListener) {
//            //
//            //                    }
//
//            emitter.setDisposable(Disposables.fromRunnable(Runnable {
//                Log.d("RealmListener", "Removed")
//                results.removeChangeListener(listener)
//                realm.close()
//            }))
//
//            results.addChangeListener(listener)
//            realm.executeTransaction { realm -> emitter.onNext(realm.copyFromRealm(results)) }

//    })
    }

    fun save(player: Player): Completable {
        return Completable.create({ subscriber ->
            realm.executeTransaction {
                val playerObject = realm.createObject(Player::class.java, UUID.randomUUID().toString())
                playerObject.coins = player.coins
                playerObject.experience = player.experience
                player.id = playerObject.id
                subscriber.onComplete()
            }
        })

    }
}