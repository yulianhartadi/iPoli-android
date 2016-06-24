package io.ipoli.android.challenge.events;

import io.ipoli.android.challenge.data.Challenge;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/24/16.
 */
public class NewChallengeEvent {
    public final Challenge challenge;

    public NewChallengeEvent(Challenge challenge) {
        this.challenge = challenge;
    }
}
