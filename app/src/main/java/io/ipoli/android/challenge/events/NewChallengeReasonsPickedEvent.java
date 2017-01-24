package io.ipoli.android.challenge.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/24/17.
 */
public class NewChallengeReasonsPickedEvent {
    public final String reason1;
    public final String reason2;
    public final String reason3;

    public NewChallengeReasonsPickedEvent(String reason1, String reason2, String reason3) {
        this.reason1 = reason1;
        this.reason2 = reason2;
        this.reason3 = reason3;
    }
}
