package io.ipoli.android.quest.persistence;

import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/10/16.
 */
public interface OnDatabaseChangedListener<T> {
    void onDatabaseChanged(List<T> results);
}
