package io.ipoli.android.app.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/15/16.
 */
public class PlayerCreatedEvent {
    public String id;

    public PlayerCreatedEvent(String id) {
        this.id = id;
    }
}
