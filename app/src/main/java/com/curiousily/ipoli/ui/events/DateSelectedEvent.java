package com.curiousily.ipoli.ui.events;

import java.util.Date;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/23/15.
 */
public class DateSelectedEvent {
    public final Date date;

    public DateSelectedEvent(Date date) {
        this.date = date;
    }
}
