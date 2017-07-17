package io.ipoli.android.store.events;

import io.ipoli.android.store.PowerUp;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/30/17.
 */

public class PowerUpUnlockedEvent {
    public final PowerUp powerUp;

    public PowerUpUnlockedEvent(PowerUp powerUp) {
        this.powerUp = powerUp;
    }
}
