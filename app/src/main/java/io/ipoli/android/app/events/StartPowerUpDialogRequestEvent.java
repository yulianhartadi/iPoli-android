package io.ipoli.android.app.events;

import io.ipoli.android.store.PowerUp;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/31/17.
 */

public class StartPowerUpDialogRequestEvent {
    public final PowerUp powerUp;

    public StartPowerUpDialogRequestEvent(PowerUp powerUp) {
        this.powerUp = powerUp;
    }
}
