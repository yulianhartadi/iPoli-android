package io.ipoli.android.app.events;

import io.ipoli.android.store.Upgrade;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/31/17.
 */

public class StartUpgradeDialogRequestEvent {
    public final Upgrade upgrade;

    public StartUpgradeDialogRequestEvent(Upgrade upgrade) {
        this.upgrade = upgrade;
    }
}
