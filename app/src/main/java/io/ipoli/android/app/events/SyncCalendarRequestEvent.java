package io.ipoli.android.app.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/14/16.
 */
public class SyncCalendarRequestEvent {
    public EventSource source;

    public SyncCalendarRequestEvent(EventSource source) {
        this.source = source;
    }
}
