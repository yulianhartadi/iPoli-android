package io.ipoli.android.app.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/27/17.
 */
public class TimeFormatChangedEvent {
    public final boolean use24HourFormat;

    public TimeFormatChangedEvent(boolean use24HourFormat) {
        this.use24HourFormat = use24HourFormat;
    }
}
