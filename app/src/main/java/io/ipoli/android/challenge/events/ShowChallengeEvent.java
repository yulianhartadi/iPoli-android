package io.ipoli.android.challenge.events;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.challenge.data.Challenge;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/13/16.
 */
public class ShowChallengeEvent {
    public final Challenge challenge;
    public final EventSource source;

    public ShowChallengeEvent(Challenge challenge, EventSource source) {
        this.challenge = challenge;
        this.source = source;
    }
}
