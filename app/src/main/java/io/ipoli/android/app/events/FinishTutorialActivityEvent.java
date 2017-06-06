package io.ipoli.android.app.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/23/17.
 */

public class FinishTutorialActivityEvent {

    public final String playerName;

    public FinishTutorialActivityEvent(String playerName) {

        this.playerName = playerName;
    }
}
