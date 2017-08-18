package io.ipoli.android.reward;

import android.util.Log;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import timber.log.Timber;

/**
 * Created by Venelin Valkov <venelin@curiousily.com> on 7/8/17.
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

    public static Observable<List<Reward>> loadRewards() {
        return Observable.create(new ObservableOnSubscribe<List<Reward>>() {
            @Override
            public void subscribe(final ObservableEmitter<List<Reward>> emitter)
                    throws Exception {
                final Realm realm = Realm.getDefaultInstance();
                final RealmResults<Reward> results = realm.where(Reward.class).findAll();
//                final RealmResults<Reward> results = realm.where(Reward.class).findAllAsync();

//                final RealmChangeListener<RealmResults<Reward>> listener = _realm -> {
//                    if (!emitter.isUnsubscribed()) {
//                        emitter.onNext(results);
//                    }
//                };

                final RealmChangeListener<RealmResults<Reward>> listener = new RealmChangeListener<RealmResults<Reward>>() {
                    @Override
                    public void onChange(final RealmResults<Reward> rewards) {
                        if (!emitter.isDisposed()) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                            emitter.onNext(realm.copyFromRealm(rewards));
                                }
                            });
                        }
                    }

//                    @Override
//                    public void onChange(Realm realmListener) {
//
//                    }
                };

                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("RealmListener", "Removed");
                        results.removeChangeListener(listener);
                        realm.close();
                    }
                }));

                results.addChangeListener(listener);
//                realm.executeTransaction(new Realm.Transaction() {
//                    @Override
//                    public void execute(Realm realm) {
                Timber.d("Emitting");
                emitter.onNext(realm.copyFromRealm(results));
//                    }
//                });
            }
        });
//                .subscribeOn(Schedulers.newThread())
//                .unsubscribeOn(AndroidSchedulers.mainThread());
    }
}