package io.ipoli.android.app.persistence;

import java.util.List;

import io.ipoli.android.quest.persistence.OnChangeListener;
import io.ipoli.android.quest.persistence.OnDataChangedListener;
import io.ipoli.android.quest.persistence.OnOperationCompletedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/30/16.
 */
public interface PersistenceService<T extends PersistedObject> {

    void save(T obj);

    void save(T obj, OnOperationCompletedListener listener);

    void save(List<T> objects);

    void save(List<T> objects, OnOperationCompletedListener listener);

    void findById(String id, OnDataChangedListener<T> listener);

    void listenById(String id, OnDataChangedListener<T> listener);

    void delete(T object);

    void delete(T object, OnOperationCompletedListener listener);

    void delete(List<T> objects);

    void delete(List<T> objects, OnOperationCompletedListener listener);

    void listenForChange(OnChangeListener<List<T>> listener);

    void removeAllListeners();

    void removeDataChangedListener(OnDataChangedListener<?> listener);

    void listenForAll(OnDataChangedListener<List<T>> listener);
}
