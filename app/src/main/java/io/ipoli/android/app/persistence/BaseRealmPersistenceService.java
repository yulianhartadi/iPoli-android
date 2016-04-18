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
        return save(object, true);
    }

    public Observable<T> save(T object, boolean markUpdated) {
        if (markUpdated) {
            object.markUpdated();
        }
        Realm realm = getRealm();
        realm.beginTransaction();
        T res = realm.copyFromRealm(realm.copyToRealmOrUpdate(object));
        realm.commitTransaction();
        onObjectSaved(res);
        return Observable.just(res);
    }

    public Observable<List<T>> saveAll(List<T> objects) {
        return saveAll(objects, true);
    }

    public Observable<List<T>> saveAll(List<T> objects, boolean markUpdated) {
        if (markUpdated) {
            for (T o : objects) {
                o.markUpdated();
            }
        }
        Realm realm = getRealm();
        realm.beginTransaction();
        List<T> res = realm.copyFromRealm(realm.copyToRealmOrUpdate(objects));
        realm.commitTransaction();
        onObjectsSaved(res);
        return Observable.just(res);
    }

    public Observable<T> findById(String id) {
        return fromRealm(where().equalTo("id", id).findFirst());
    }

    public Observable<List<T>> findAllModifiedAfter(Date dateTime) {
        return fromRealm(where().greaterThan("updatedAt", dateTime).findAll());
    }

    protected void onObjectSaved(T obj) {
    }

    protected void onObjectsSaved(List<T> objs) {
    }

    protected abstract Class<T> getRealmObjectClass();

    protected Observable<T> fromRealm(T obj) {
        if (obj == null) {
            return Observable.just(null);
        }
        return Observable.just(getRealm().copyFromRealm(obj));
    }

    public void updateId(T obj, String newId) {
        Realm realm = getRealm();
        realm.beginTransaction();
        T realmObj = realm.copyToRealmOrUpdate(obj);
        realmObj.setId(newId);
        realm.commitTransaction();
    }

    protected Observable<List<T>> fromRealm(List<T> objs) {
        return Observable.just(getRealm().copyFromRealm(objs));
    }

    protected Realm getRealm() {
        return Realm.getDefaultInstance();
    }
}
