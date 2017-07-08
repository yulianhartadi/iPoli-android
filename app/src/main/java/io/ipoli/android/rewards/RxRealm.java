package io.ipoli.android.rewards;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposables;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by vini on 7/8/17.
 */
public class RxRealm {
//    public static Flowable<Realm> getRealm(final Realm realm) {
//        return Flowable.create(new FlowableOnSubscribe<Realm>() {
//            @Override
//            public void subscribe(final FlowableEmitter<Realm> emitter)
//                    throws Exception {
//                final Realm observableRealm = Realm.getInstance(realm.getConfiguration());
//
//                final RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
//                    @Override
//                    public void onChange(Realm realmListener) {
//                        emitter.onNext(realmListener);
//                    }
//                };
//
//                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
//                    @Override
//                    public void run() {
//                        observableRealm.removeChangeListener(listener);
//                        observableRealm.close();
//                    }
//                }));
//
//                observableRealm.addChangeListener(listener);
//                emitter.onNext(observableRealm);
//            }
//        }, BackpressureStrategy.LATEST);
//    }

    public static Observable<RealmResults<Reward>> loadRewards() {
        return Observable.create(new ObservableOnSubscribe<RealmResults<Reward>>() {
            @Override
            public void subscribe(final ObservableEmitter<RealmResults<Reward>> emitter)
                    throws Exception {
                final Realm realm = Realm.getDefaultInstance();
                final RealmResults<Reward> results = realm.where(Reward.class).findAll();

//                final RealmChangeListener<RealmResults<Reward>> listener = _realm -> {
//                    if (!emitter.isUnsubscribed()) {
//                        emitter.onNext(results);
//                    }
//                };

                final RealmChangeListener<RealmResults<Reward>> listener = new RealmChangeListener<RealmResults<Reward>>() {
                    @Override
                    public void onChange(RealmResults<Reward> rewards) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(rewards);
                        }
                    }

//                    @Override
//                    public void onChange(Realm realmListener) {
//
//                    }
                };

//                emitter.setDisposable(Disposables.fromRunnable(() -> {
//                    results.removeChangeListener(listener);
//                    realm.close();
//                }));

                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        results.removeChangeListener(listener);
                        realm.close();
                    }
                }));

                results.addChangeListener(listener);
                emitter.onNext(results);
            }
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(AndroidSchedulers.mainThread());
    }
}
