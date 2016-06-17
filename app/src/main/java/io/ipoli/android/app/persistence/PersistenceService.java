package io.ipoli.android.app.persistence;

import java.util.List;

import io.ipoli.android.app.net.RemoteObject;
import io.realm.RealmObject;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/30/16.
 */
public interface PersistenceService<T extends RealmObject & RemoteObject> {

    Observable<T> save(T obj);

    Observable<List<T>> save(List<T> objects);

    void saveSync(T obj);

    void saveSync(T obj, boolean markUpdated);

    void saveSync(List<T> objects);

    void saveSync(List<T> objects, boolean markUpdated);

    Observable<T> saveRemoteObject(T object);

    Observable<List<T>> saveRemoteObjects(List<T> objects);

    List<T> findAllWhoNeedSyncWithRemote();

    T findById(String id);

    T findByRemoteId(String id);

    Observable<Void> delete(List<T> objects);

    void close();
}
