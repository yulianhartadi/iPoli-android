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

    void saveSync(T obj);

    Observable<T> saveRemoteObject(T object);

    Observable<List<T>> saveRemoteObjects(List<T> objects);

    Observable<List<T>> findAllWhoNeedSyncWithRemote();

    Observable<T> findById(String id);

    Observable<T> findByRemoteId(String id);

    void saveAllSync(List<T> objects);
}
