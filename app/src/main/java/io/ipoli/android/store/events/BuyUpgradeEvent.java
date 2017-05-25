package io.ipoli.android.store.events;

import io.ipoli.android.store.Upgrade;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class BuyUpgradeEvent {
    public final Upgrade upgrade;

    public BuyUpgradeEvent(Upgrade upgrade) {
        this.upgrade = upgrade;
    }
}
