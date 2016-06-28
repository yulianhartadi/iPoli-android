package io.ipoli.android.quest.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/24/16.
 */
public class ChallengePickedEvent {
    public final String mode;
    public final String name;

    public ChallengePickedEvent(String mode, String name) {
        this.mode = mode;
        this.name = name;
    }
}
