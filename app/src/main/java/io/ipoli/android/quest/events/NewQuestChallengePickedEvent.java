package io.ipoli.android.quest.events;

import io.ipoli.android.challenge.data.Challenge;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/17.
 */
public class NewQuestChallengePickedEvent {
    public final Challenge challenge;

    public NewQuestChallengePickedEvent(Challenge challenge) {
        this.challenge = challenge;
    }
}
