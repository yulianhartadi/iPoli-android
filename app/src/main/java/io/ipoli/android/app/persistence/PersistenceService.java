package io.ipoli.android.app.persistence;

import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/30/16.
 */
public interface PersistenceService<T extends PersistedObject> {

    void save(T obj);

    void findById(String id, OnDataChangedListener<T> listener);

    void listenById(String id, OnDataChangedListener<T> listener);

    void delete(T object);

    void removeAllListeners();

    void removeDataChangedListener(OnDataChangedListener<?> listener);
}
