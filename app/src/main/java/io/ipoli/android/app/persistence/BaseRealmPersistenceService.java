package io.ipoli.android.app.persistence;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

import io.ipoli.android.app.net.RemoteObject;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
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
        return find(where -> where.equalTo("id", id).findFirstAsync());
    }

    public Observable<List<T>> findAllWhoNeedSyncWithRemote() {
        return findAll(where -> where.equalTo("needsSyncWithRemote", true).findAllAsync());
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
        String id = obj.getId();
        Realm realm = getRealm();
        T realmObj = realm.where(getRealmObjectClass())
                .equalTo("id", id)
                .findFirst();

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
    }

    protected void onObjectDeleted(String id) {

    }

    protected Observable<List<T>> fromRealm(List<T> objs) {
        return Observable.just(getRealm().copyFromRealm(objs));
    }

    protected Realm getRealm() {
        return Realm.getDefaultInstance();
    }

    protected Observable<List<T>> findAll(RealmFindAllQueryBuilder<T> queryBuilder) {
        return new RealmFindAllCommand(queryBuilder).execute();
    }

    protected Observable<T> find(RealmFindQueryBuilder<T> queryBuilder) {
        return new RealmFindCommand(queryBuilder).execute();
    }

    public interface RealmFindQueryBuilder<T extends RealmObject & RemoteObject> {
        T buildQuery(RealmQuery<T> where);
    }

    public interface RealmFindAllQueryBuilder<T extends RealmObject & RemoteObject> {
        RealmResults<T> buildQuery(RealmQuery<T> where);
    }

    public class RealmFindAllCommand {

        private RealmFindAllQueryBuilder<T> queryBuilder;

        public RealmFindAllCommand(RealmFindAllQueryBuilder<T> queryBuilder) {
            this.queryBuilder = queryBuilder;
        }

        public Observable<List<T>> execute() {
            try (Realm realm = getRealm()) {
                return queryBuilder.buildQuery(realm.where(getRealmObjectClass()))
                        .asObservable()
                        .filter(RealmResults::isLoaded)
                        .map(realm::copyFromRealm);
            }
        }
    }

    public class RealmFindCommand {

        private RealmFindQueryBuilder<T> queryBuilder;

        public RealmFindCommand(RealmFindQueryBuilder<T> queryBuilder) {
            this.queryBuilder = queryBuilder;
        }

        public Observable<T> execute() {
            try (Realm realm = getRealm()) {
                return queryBuilder.buildQuery(realm.where(getRealmObjectClass()))
                        .asObservable()
                        .filter(T::isLoaded)
                        .map(realmObject -> {
                            if (!realmObject.isValid()) {
                                return null;
                            }
                            return realm.copyFromRealm((T) realmObject);
                        });
            }
        }
    }


}
