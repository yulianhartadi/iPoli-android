package io.ipoli.android.app.persistence;

import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/30/16.
 */
public interface PersistenceService<T extends PersistedObject> {

    void save(T obj);

    void save(List<T> objects);

    void findById(String id, OnDataChangedListener<T> listener);

    void listenById(String id, OnDataChangedListener<T> listener);

    void delete(T object);

    void removeAllListeners();
}
