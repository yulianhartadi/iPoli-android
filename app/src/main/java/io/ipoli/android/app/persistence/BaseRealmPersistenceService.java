package io.ipoli.android.app.persistence;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public class BaseRealmPersistenceService<T extends RealmObject> {

    protected RealmQuery<T> where(Class<T> clazz) {
        return getRealm().where(clazz);
    }

    public Observable<T> save(T obj) {
        Realm realm = getRealm();
        realm.beginTransaction();
        T res = realm.copyFromRealm(realm.copyToRealmOrUpdate(obj));
        realm.commitTransaction();
        return Observable.just(res);
    }

    public Observable<List<T>> saveAll(List<T> objs) {
        Realm realm = getRealm();
        realm.beginTransaction();
        List<T> res = realm.copyFromRealm(realm.copyToRealmOrUpdate(objs));
        realm.commitTransaction();
        return Observable.just(res);
    }

    private Realm getRealm() {
        return Realm.getDefaultInstance();
    }
}
