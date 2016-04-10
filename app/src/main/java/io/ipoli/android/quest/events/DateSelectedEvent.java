package io.ipoli.android.quest.events;

import java.util.Date;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/24/16.
 */
public class DateSelectedEvent {
    public Date date;

    public DateSelectedEvent(Date date) {
        this.date = date;
    }
}
