package io.ipoli.android.app.events;

import android.app.Activity;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/15/16.
 */
public class ScreenShownEvent {
    public final EventSource source;
    public final Activity activity;

    public ScreenShownEvent(Activity activity, EventSource source) {
        this.activity = activity;
        this.source = source;
    }
}
