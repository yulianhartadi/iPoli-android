package io.ipoli.android.store.events;

import io.ipoli.android.store.StoreItemType;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class StoreItemSelectedEvent {
    public final StoreItemType type;

    public StoreItemSelectedEvent(StoreItemType type) {
        this.type = type;
    }
}
