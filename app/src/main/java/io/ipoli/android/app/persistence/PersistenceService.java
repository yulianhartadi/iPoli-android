package io.ipoli.android.app.persistence;

import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/30/16.
 */
public interface PersistenceService<T extends PersistedObject> {

    void save(T obj);

    void save(List<T> objects);

    T findById(String id);

    void delete(List<T> objects);

    void removeAllListeners();
}
