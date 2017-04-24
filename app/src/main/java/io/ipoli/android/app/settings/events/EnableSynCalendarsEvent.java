package io.ipoli.android.app.settings.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/24/17.
 */

public class EnableSynCalendarsEvent {
    public final boolean isEnabled;

    public EnableSynCalendarsEvent(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
