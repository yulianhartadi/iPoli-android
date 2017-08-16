package io.ipoli.android.player

import io.reactivex.Completable
import io.reactivex.Observable
import io.realm.Realm
import java.util.*

/**
 * Created by Venelin Valkov <venelin@curiousily.com> on 8/2/17.
 */
class PlayerRepository {

    val realm: Realm = Realm.getDefaultInstance()

    fun get(): Observable<Player> {
        return Observable.create({ emitter ->
            val realm = Realm.getDefaultInstance()
            emitter.onNext(realm.copyFromRealm(realm.where(Player::class.java).findFirst()))
            emitter.onComplete()
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
                player.id = UUID.randomUUID().toString()
                realm.copyToRealmOrUpdate(player)
                subscriber.onComplete()
            }
        })

    }
}