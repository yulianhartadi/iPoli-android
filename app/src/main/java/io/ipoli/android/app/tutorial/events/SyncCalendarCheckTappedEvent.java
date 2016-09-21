package io.ipoli.android.app.tutorial.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/14/16.
 */
public class SyncCalendarCheckTappedEvent {
    public final boolean isChecked;

    public SyncCalendarCheckTappedEvent(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
