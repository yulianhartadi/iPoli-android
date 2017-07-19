package io.ipoli.android.store.events;

import io.ipoli.android.store.PowerUp;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class BuyPowerUpEvent {
    public final PowerUp powerUp;

    public BuyPowerUpEvent(PowerUp powerUp) {
        this.powerUp = powerUp;
    }
}
