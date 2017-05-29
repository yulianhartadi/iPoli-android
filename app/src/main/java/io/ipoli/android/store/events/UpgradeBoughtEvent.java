package io.ipoli.android.store.events;

import io.ipoli.android.store.Upgrade;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/30/17.
 */

public class UpgradeBoughtEvent {
    public final Upgrade upgrade;

    public UpgradeBoughtEvent(Upgrade upgrade) {
        this.upgrade = upgrade;
    }
}
