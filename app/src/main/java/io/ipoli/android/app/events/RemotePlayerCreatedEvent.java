package io.ipoli.android.app.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/15/16.
 */
public class RemotePlayerCreatedEvent {
    public String playerId;

    public RemotePlayerCreatedEvent(String playerId) {
        this.playerId = playerId;
    }
}
