package io.ipoli.android.app.persistence;

import java.util.Date;
import java.util.List;

import io.realm.RealmObject;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/30/16.
 */
public interface PersistenceService<T extends RealmObject> {

    Observable<T> save(T obj);

    Observable<T> save(T obj, boolean markUpdated);

    Observable<List<T>> saveAll(List<T> objs);

    Observable<List<T>> saveAll(List<T> objs, boolean markUpdated);

    public Observable<List<T>> findAllModifiedAfter(Date dateTime);

    void updateId(T obj, String newId);

    Observable<T> findById(String id);
}
