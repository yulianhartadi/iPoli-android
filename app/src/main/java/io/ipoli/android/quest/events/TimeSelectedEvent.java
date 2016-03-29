package io.ipoli.android.quest.events;

import io.ipoli.android.app.utils.Time;

/**
 * Created by Naughty Spirit <hi@naughtyspirit.co>
 * on 1/24/16.
 */
public class TimeSelectedEvent {
    public Time time;

    public TimeSelectedEvent(Time time) {
        this.time = time;
    }
}
