package io.ipoli.android.challenge.ui.events;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.challenge.data.Challenge;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/24/16.
 */
public class UpdateChallengeEvent {
    public final Challenge challenge;
    public final EventSource source;

    public UpdateChallengeEvent(Challenge challenge, EventSource source) {
        this.challenge = challenge;
        this.source = source;
    }
}
