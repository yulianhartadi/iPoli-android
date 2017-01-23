package io.ipoli.android.challenge.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/23/17.
 */
public class NewChallengeResultsPickedEvent {
    public final String result1;
    public final String result2;
    public final String result3;

    public NewChallengeResultsPickedEvent(String result1, String result2, String result3) {
        this.result1 = result1;
        this.result2 = result2;
        this.result3 = result3;
    }
}
