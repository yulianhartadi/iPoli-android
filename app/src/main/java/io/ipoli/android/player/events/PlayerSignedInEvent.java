package io.ipoli.android.player.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/30/17.
 */

public class PlayerSignedInEvent {
    public final String provider;
    public final boolean isNew;

    public PlayerSignedInEvent(String provider, boolean isNew) {
        this.provider = provider;
        this.isNew = isNew;
    }
}
