package com.curiousily.ipoli.ui.events;

import java.util.Date;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/23/15.
 */
public class TimeSelectedEvent {
    public final Date time;

    public TimeSelectedEvent(Date time) {
        this.time = time;
    }
}
