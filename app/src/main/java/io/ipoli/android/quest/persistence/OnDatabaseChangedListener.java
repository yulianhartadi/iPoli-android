package io.ipoli.android.quest.persistence;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/10/16.
 */
public interface OnDatabaseChangedListener<T> {
    void onDatabaseChanged(T result);
}
