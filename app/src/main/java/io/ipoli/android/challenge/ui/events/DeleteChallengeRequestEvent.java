package io.ipoli.android.challenge.ui.events;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.challenge.data.Challenge;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/24/16.
 */
public class DeleteChallengeRequestEvent {
    public final Challenge challenge;
    public final boolean shouldDeleteQuests;
    public final EventSource source;

    public DeleteChallengeRequestEvent(Challenge challenge, boolean shouldDeleteQuests, EventSource source) {
        this.challenge = challenge;
        this.shouldDeleteQuests = shouldDeleteQuests;
        this.source = source;
    }
}
