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

    protected void listenForChanges(RealmResults<T> results, OnDatabaseChangedListener<T> listener) {
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

    @Override
    public Observable<List<T>> save(List<T> objects) {
        for (T obj : objects) {
            obj.markUpdated();
        }
        return Observable.create(subscriber -> {
            realm.executeTransactionAsync(backgroundRealm ->
                            backgroundRealm.copyToRealmOrUpdate(objects),
                    () -> {
                        subscriber.onNext(objects);
                        subscriber.onCompleted();
                        onObjectsSaved(objects);
                    }, subscriber::onError);
        });
    }

    protected void onObjectsSaved(List<T> objects) {

    }

    protected void onObjectSaved(T object) {

    }

    @Override
    public void saveSync(T obj) {
        saveSync(obj, true);
    }

    @Override
    public void saveSync(T obj, boolean markUpdated) {
        if (markUpdated) {
            obj.markUpdated();
        }
        realm.executeTransaction(transactionRealm ->
                transactionRealm.copyToRealmOrUpdate(obj));
    }

    @Override
    public void saveSync(List<T> objects) {
        saveSync(objects, true);
    }

    @Override
    public void saveSync(List<T> objects, boolean markUpdated) {
        if (objects.isEmpty()) {
            return;
        }
        if (markUpdated) {
            for (T obj : objects) {
                obj.markUpdated();
            }
        }
        realm.executeTransaction(transactionRealm ->
                transactionRealm.copyToRealmOrUpdate(objects));
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
    public Observable<Void> delete(List<T> objects) {
        if (objects.isEmpty()) {
            return Observable.empty();
        }
        return Observable.create(subscriber -> {
            Realm realm = getRealm();
            realm.executeTransactionAsync(backgroundRealm -> {
                        RealmQuery<T> q = backgroundRealm.where(getRealmObjectClass());
                        for (int i = 0; i < objects.size(); i++) {
                            if (i > 0) {
                                q = q.or();
                            }
                            q = q.equalTo("id", objects.get(i).getId());
                        }
                        RealmResults<T> results = q.findAll();
                        results.deleteAllFromRealm();
                    },
                    () -> {
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                    }, subscriber::onError);
        });
    }

    @Override
    public T findById(String id) {
        return findOne(where -> where.equalTo("id", id).findFirst());
    }

    @Override
    public T findByRemoteId(String id) {
        return findOne(where -> where.equalTo("remoteId", id).findFirst());
    }

    @Override
    public List<T> findAllWhoNeedSyncWithRemote() {
        return findAllIncludingDeleted(where -> where
                .equalTo("needsSyncWithRemote", true)
                .findAll());
    }

    protected abstract Class<T> getRealmObjectClass();

    protected Realm getRealm() {
        return realm;
    }

    protected T findOne(RealmFindOneQueryBuilder<T> queryBuilder) {
        return new RealmFindOneCommand<T>(queryBuilder, where(), getRealm()).execute();
    }

    protected List<T> findAll(RealmFindAllQueryBuilder<T> queryBuilder) {
        return new RealmFindAllCommand<T>(queryBuilder, where(), getRealm()).execute();
    }

    protected List<T> findAllIncludingDeleted(RealmFindAllQueryBuilder<T> queryBuilder) {
        return new RealmFindAllCommand<T>(queryBuilder, whereIncludingDeleted(), getRealm()).execute();
    }

    @Override
    public void removeAllListeners() {
        for (RealmResults<?> res : realmResults) {
            res.removeChangeListeners();
        }
        realmResults.clear();
    }
}
