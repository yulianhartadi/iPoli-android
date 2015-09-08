package com.curiousily.ipoli.schedule.events;

import com.curiousily.ipoli.schedule.DailySchedule;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/8/15.
 */
public class UpdateDailyScheduleEvent {

    public final DailySchedule schedule;

    public UpdateDailyScheduleEvent(DailySchedule schedule) {
        this.schedule = schedule;
    }
}
