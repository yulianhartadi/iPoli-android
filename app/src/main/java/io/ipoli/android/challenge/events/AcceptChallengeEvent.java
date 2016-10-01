package io.ipoli.android.challenge.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 10/1/16.
 */
public class AcceptChallengeEvent {
    public final String name;

    public AcceptChallengeEvent(String name) {
        this.name = name;
    }
}
