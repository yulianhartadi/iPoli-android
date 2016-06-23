package io.ipoli.android.challenge.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/22/16.
 */
public class DailyChallengeQuestsSelectedEvent {
    public final int count;

    public DailyChallengeQuestsSelectedEvent(int count) {
        this.count = count;
    }
}
