package io.ipoli.android.quest.events;

import java.util.Date;

/**
 * Created by Naughty Spirit <hi@naughtyspirit.co>
 * on 1/24/16.
 */
public class DateSelectedEvent {
    public Date date;

    public DateSelectedEvent(Date date) {
        this.date = date;
    }
}
