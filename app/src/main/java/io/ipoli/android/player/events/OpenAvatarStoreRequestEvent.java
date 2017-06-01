package io.ipoli.android.player.events;

import io.ipoli.android.app.events.EventSource;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/22/16.
 */
public class OpenAvatarStoreRequestEvent {
    public EventSource source;

    public OpenAvatarStoreRequestEvent(EventSource source) {
        this.source = source;
    }
}
