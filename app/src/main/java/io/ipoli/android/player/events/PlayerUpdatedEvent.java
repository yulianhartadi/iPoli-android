package io.ipoli.android.player.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/17.
 */

public class PlayerUpdatedEvent {
    public final String playerId;

    public PlayerUpdatedEvent(String playerId) {
        this.playerId = playerId;
    }
}
