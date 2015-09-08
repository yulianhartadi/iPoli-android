package com.curiousily.ipoli.schedule.events;

import java.util.Date;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/16/15.
 */
public class LoadDailyScheduleEvent {

    public final Date scheduledFor;
    public final String userId;

    public LoadDailyScheduleEvent(Date scheduledFor, String userId) {
        this.scheduledFor = scheduledFor;
        this.userId = userId;
    }
}
