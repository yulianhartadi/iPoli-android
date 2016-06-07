package io.ipoli.android.app.persistence;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

import io.ipoli.android.app.net.RemoteObject;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public abstract class BaseRealmPersistenceService<T extends RealmObject & RemoteObject> {

    protected static Date toUTCDateAtStartOfDay(LocalDate startDate) {
        return startDate.toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
    }

    protected RealmQuery<T> where() {
        return getRealm().where(getRealmObjectClass());
    }

    public Observable<T> save(T object) {
        object.markUpdated();
        return Observable.create(subscriber -> {
            Realm realm = getRealm();
            realm.executeTransactionAsync(backgroundRealm ->
                            backgroundRealm.copyToRealmOrUpdate(object),
                    () -> {
                        subscriber.onNext(object);
                        subscriber.onCompleted();
                        onObjectSaved(object);
                        realm.close();
                    }, error -> {
                        subscriber.onError(error);
                        realm.close();
                    });
        });
    }

    protected void onObjectSaved(T object) {

    }

    public void saveSync(T obj) {
        obj.markUpdated();
        try (Realm realm = getRealm()) {
            realm.executeTransaction(transactionRealm ->
                    transactionRealm.copyToRealmOrUpdate(obj));
        }
    }

    public void saveAllSync(List<T> objects) {
        for(T obj: objects) {
            obj.markUpdated();
        }
        try (Realm realm = getRealm()) {
            realm.executeTransaction(transactionRealm ->
                    transactionRealm.copyToRealmOrUpdate(objects));
        }
    }

    public Observable<T> saveRemoteObject(T object) {
        return Observable.create(subscriber -> {
            Realm realm = getRealm();
            realm.executeTransactionAsync(backgroundRealm ->
                            backgroundRealm.copyToRealmOrUpdate(object),
                    () -> {
                        subscriber.onNext(object);
                        subscriber.onCompleted();
                        realm.close();
                    }, error -> {
                        subscriber.onError(error);
                        realm.close();
                    });
        });
    }

    public Observable<List<T>> saveRemoteObjects(List<T> objects) {
        if (objects.isEmpty()) {
            return Observable.defer(() -> Observable.just(objects));
        }
        return Observable.create(subscriber -> {
            Realm realm = getRealm();
            realm.executeTransactionAsync(backgroundRealm ->
                            backgroundRealm.copyToRealmOrUpdate(objects),
                    () -> {
                        subscriber.onNext(objects);
                        subscriber.onCompleted();
                        realm.close();
                    }, error -> {
                        subscriber.onError(error);
                        realm.close();
                    });
        });
    }

    public Observable<T> findById(String id) {
        return find(where -> where.equalTo("id", id).findFirstAsync());
    }

    public Observable<T> findByRemoteId(String id) {
        return find(where -> where.equalTo("remoteId", id).findFirstAsync());
    }

    public Observable<List<T>> findAllWhoNeedSyncWithRemote() {
        return findAll(where -> where.equalTo("needsSyncWithRemote", true).findAllAsync());
    }

    protected abstract Class<T> getRealmObjectClass();

    public Observable<String> delete(T obj) {
        if (obj == null) {
            return Observable.empty();
        }
        String id = obj.getId();
        Realm realm = getRealm();

        return findById(id).flatMap(realmObj -> {
            if (realmObj == null) {
                realm.close();
                return Observable.empty();
            }
            return Observable.create(subscriber -> {
                realm.executeTransactionAsync(backgroundRealm -> {
                            T objToDelete = backgroundRealm.where(getRealmObjectClass())
                                    .equalTo("id", id)
                                    .findFirst();
                            objToDelete.deleteFromRealm();
                        },
                        () -> {
                            subscriber.onNext(id);
                            subscriber.onCompleted();
                            onObjectDeleted(id);
                            realm.close();
                        }, error -> {
                            subscriber.onError(error);
                            realm.close();
                        });
            });
        });


    }

    protected void onObjectDeleted(String id) {

    }

    protected Realm getRealm() {
        return Realm.getDefaultInstance();
    }

    protected Observable<List<T>> findAll(RealmFindAllQueryBuilder<T> queryBuilder) {
        return new RealmFindAllCommand<T>(queryBuilder, getRealmObjectClass()).execute();
    }

    protected Observable<T> find(RealmFindQueryBuilder<T> queryBuilder) {
        return new RealmFindCommand<T>(queryBuilder, getRealmObjectClass()).execute();
    }

}
