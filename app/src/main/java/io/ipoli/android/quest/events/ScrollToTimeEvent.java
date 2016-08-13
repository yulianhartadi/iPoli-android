package io.ipoli.android.quest.events;

import io.ipoli.android.app.utils.Time;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/13/16.
 */
public class ScrollToTimeEvent {
    public final Time time;

    public ScrollToTimeEvent(Time time) {
        this.time = time;
    }
}
