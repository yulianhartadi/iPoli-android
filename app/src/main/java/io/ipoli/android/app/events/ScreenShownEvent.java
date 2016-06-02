package io.ipoli.android.app.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/15/16.
 */
public class ScreenShownEvent {
    public EventSource source;

    public ScreenShownEvent(EventSource source) {
        this.source = source;
    }
}
