package io.ipoli.android.app.persistence;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.app.net.RemoteObject;
import io.ipoli.android.quest.persistence.OnDatabaseChangedListener;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public abstract class BaseRealmPersistenceService<T extends RealmObject & RemoteObject> implements PersistenceService<T> {

    private final Realm realm;
    private List<RealmResults<?>> realmResults;

    public BaseRealmPersistenceService(Realm realm) {
        this.realm = realm;
        this.realmResults = new ArrayList<>();
    }

    protected void listenForResults(RealmResults<T> results, OnDatabaseChangedListener<T> listener) {
        realmResults.add(results);
        results.addChangeListener(element -> {
            if (element.isLoaded()) {
                listener.onDatabaseChanged(realm.copyFromRealm(element));
            }
        });
    }

    protected RealmQuery<T> where() {
        return realm.where(getRealmObjectClass()).equalTo("isDeleted", false);
    }

    protected RealmQuery<T> whereIncludingDeleted() {
        return realm.where(getRealmObjectClass());
    }

    @Override
    public Observable<T> save(T object) {
        object.markUpdated();
        return Observable.create(subscriber -> {
            realm.executeTransactionAsync(backgroundRealm ->
                            backgroundRealm.copyToRealmOrUpdate(object),
                    () -> {
                        subscriber.onNext(object);
                        subscriber.onCompleted();
                        onObjectSaved(object);
                    }, subscriber::onError);
        });
    }

    protected void onObjectSaved(T object) {

    }

    @Override
    public void saveSync(T obj) {
        obj.markUpdated();
        try (Realm realm = getRealm()) {
            realm.executeTransaction(transactionRealm ->
                    transactionRealm.copyToRealmOrUpdate(obj));
        }
    }

    @Override
    public void saveSync(List<T> objects) {
        for (T obj : objects) {
            obj.markUpdated();
        }
        try (Realm realm = getRealm()) {
            realm.executeTransaction(transactionRealm ->
                    transactionRealm.copyToRealmOrUpdate(objects));
        }
    }

    @Override
    public Observable<T> saveRemoteObject(T object) {
        return Observable.create(subscriber -> {
            Realm realm = getRealm();
            realm.executeTransactionAsync(backgroundRealm ->
                            backgroundRealm.copyToRealmOrUpdate(object),
                    () -> {
                        subscriber.onNext(object);
                        subscriber.onCompleted();
                    }, subscriber::onError);
        });
    }

    @Override
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
                    }, subscriber::onError);
        });
    }

    @Override
    public T findById(String id) {
        T result = where().equalTo("id", id).findFirst();
        if (result == null) {
            return null;
        }
        return realm.copyFromRealm(result);
    }

    @Override
    public T findByRemoteIdSync(String id) {
        Realm realm = getRealm();
        T obj = realm.where(getRealmObjectClass())
                .equalTo("remoteId", id)
                .findFirst();
        if (obj == null) {
            return null;
        }
        return realm.copyFromRealm(obj);
    }

    @Override
    public List<T> findAllWhoNeedSyncWithRemote() {
        return realm.copyFromRealm(where().equalTo("needsSyncWithRemote", true).findAll());
    }

    protected abstract Class<T> getRealmObjectClass();

    protected Realm getRealm() {
        return realm;
    }

    protected Observable<List<T>> findAll(RealmFindAllQueryBuilder<T> queryBuilder) {
        return new RealmFindAllCommand<T>(queryBuilder, getRealmObjectClass()).execute();
    }

    protected Observable<List<T>> findAllIncludingDeleted(RealmFindAllQueryBuilder<T> queryBuilder) {
        return new RealmFindAllCommand<T>(queryBuilder, getRealmObjectClass(), true).execute();
    }

    protected Observable<T> find(RealmFindQueryBuilder<T> queryBuilder) {
        return new RealmFindCommand<T>(queryBuilder, getRealmObjectClass()).execute();
    }

    @Override
    public void close() {
        for (RealmResults<?> res : realmResults) {
            res.removeChangeListeners();
        }
        realmResults.clear();
    }
}
