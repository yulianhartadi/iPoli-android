package io.ipoli.android.app.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/29/17.
 */

public class PlayerMigratedEvent {
    public final String firebasePlayerId;
    public final String playerId;

    public PlayerMigratedEvent(String firebasePlayerId, String playerId) {
        this.firebasePlayerId = firebasePlayerId;
        this.playerId = playerId;
    }
}
