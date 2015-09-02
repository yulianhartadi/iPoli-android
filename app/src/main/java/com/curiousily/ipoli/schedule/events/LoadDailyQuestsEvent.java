package com.curiousily.ipoli.schedule.events;

import java.util.Date;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/16/15.
 */
public class LoadDailyQuestsEvent {

    public final Date scheduledFor;
    public final String userId;

    public LoadDailyQuestsEvent(Date scheduledFor, String userId) {
        this.scheduledFor = scheduledFor;
        this.userId = userId;
    }
}
