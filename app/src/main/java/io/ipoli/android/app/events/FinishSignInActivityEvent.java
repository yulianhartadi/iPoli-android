package io.ipoli.android.app.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/23/17.
 */

public class FinishSignInActivityEvent {
    public final boolean isNewPlayer;

    public FinishSignInActivityEvent(boolean isNewPlayer) {
        this.isNewPlayer = isNewPlayer;
    }
}
