package io.ipoli.android.challenge.events;

import io.ipoli.android.challenge.data.Difficulty;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/24/17.
 */
public class NewChallengeDifficultyPickedEvent {
    public final Difficulty difficulty;

    public NewChallengeDifficultyPickedEvent(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
}
