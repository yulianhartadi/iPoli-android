package io.ipoli.android.app.persistence;

import java.util.List;

import io.ipoli.android.app.sync.Remotable;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public abstract class BaseRealmPersistenceService<T extends RealmObject & Remotable> {

    protected RealmQuery<T> where() {
        return getRealm().where(getRealmObjectClass());
    }

    public Observable<T> save(T obj) {
        obj.markUpdated();
        Realm realm = getRealm();
        realm.beginTransaction();
        T res = realm.copyFromRealm(realm.copyToRealmOrUpdate(obj));
        realm.commitTransaction();
        onObjectSaved(res);
        return Observable.just(res);
    }

    public Observable<List<T>> saveAll(List<T> objs) {
        for (T o : objs) {
            o.markUpdated();
        }
        Realm realm = getRealm();
        realm.beginTransaction();
        List<T> res = realm.copyFromRealm(realm.copyToRealmOrUpdate(objs));
        realm.commitTransaction();
        onObjectsSaved(res);
        return Observable.just(res);
    }

    public Observable<T> findById(String id) {
        return fromRealm(where().equalTo("id", id).findFirst());
    }

    public Observable<List<T>> findAllWhoNeedSyncWithRemote() {
        return fromRealm(where().equalTo("needsSyncWithRemote", true).findAll());
    }

    protected void onObjectSaved(T obj) {
    }

    protected void onObjectsSaved(List<T> objs) {
    }

    protected abstract Class<T> getRealmObjectClass();

    protected Observable<T> fromRealm(T obj) {
        if(obj == null) {
            return Observable.just(null);
        }
        return Observable.just(getRealm().copyFromRealm(obj));
    }

    protected Observable<List<T>> fromRealm(List<T> objs) {
        return Observable.just(getRealm().copyFromRealm(objs));
    }

    protected Realm getRealm() {
        return Realm.getDefaultInstance();
    }
}
