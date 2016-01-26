package io.ipoli.android.quest.events;

import java.util.Date;

/**
 * Created by Naughty Spirit <hi@naughtyspirit.co>
 * on 1/24/16.
 */
public class TimeSelectedEvent {
    public Date time;

    public TimeSelectedEvent(Date time) {
        this.time = time;
    }
}
