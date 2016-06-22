package io.ipoli.android.player.events;

import io.ipoli.android.app.events.EventSource;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/22/16.
 */
public class PickAvatarRequestEvent {
    public EventSource source;

    public PickAvatarRequestEvent(EventSource source) {
        this.source = source;
    }
}
