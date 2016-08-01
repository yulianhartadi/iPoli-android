package io.ipoli.android.app.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/15/16.
 */
public class PlayerCreatedEvent {
    public String playerId;

    public PlayerCreatedEvent(String playerId) {
        this.playerId = playerId;
    }
}
