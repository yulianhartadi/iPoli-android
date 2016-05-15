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
        return fromRealm(where().equalTo("id", id).findFirst());
    }

    public Observable<List<T>> findAllWhoNeedSyncWithRemote() {
        return fromRealm(where().equalTo("needsSyncWithRemote", true).findAll());
    }

    protected abstract Class<T> getRealmObjectClass();

    protected Observable<T> fromRealm(T obj) {
        if (obj == null) {
            return Observable.just(null);
        }
        return Observable.just(getRealm().copyFromRealm(obj));
    }

    public Observable<Void> updateId(T obj, String newId) {
        return Observable.create(subscriber -> {
            Realm realm = getRealm();
            realm.executeTransactionAsync(realm1 -> {
                T realmObj = realm1.copyToRealmOrUpdate(obj);
                realmObj.setId(newId);
            }, () -> {
                subscriber.onNext(null);
                subscriber.onCompleted();
                realm.close();
            }, error -> {
                subscriber.onError(error);
                realm.close();
            });
        });
    }

    public Observable<String> delete(T obj) {
        if (obj == null) {
            return Observable.empty();
        }
        return Observable.create(subscriber -> {
            String id = obj.getId();
            Realm realm = getRealm();
            realm.executeTransactionAsync(backgroundRealm -> {
                T realmObj = backgroundRealm.where(getRealmObjectClass())
                        .equalTo("id", id)
                        .findFirst();
                if (realmObj == null) {
                    return;
                }
                realmObj.deleteFromRealm();
            }, () -> {
                subscriber.onNext(id);
                subscriber.onCompleted();
                onObjectDeleted(id);
                realm.close();
            }, error -> {
                subscriber.onError(error);
                realm.close();
            });
        });
    }

    protected void onObjectDeleted(String id) {

    }

    protected Observable<List<T>> fromRealm(List<T> objs) {
        return Observable.just(getRealm().copyFromRealm(objs));
    }

    protected Realm getRealm() {
        return Realm.getDefaultInstance();
    }
}
