package io.ipoli.android.store.events;

import io.ipoli.android.store.Upgrade;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/30/17.
 */

public class UpgradeUnlockedEvent {
    public final Upgrade upgrade;

    public UpgradeUnlockedEvent(Upgrade upgrade) {
        this.upgrade = upgrade;
    }
}
