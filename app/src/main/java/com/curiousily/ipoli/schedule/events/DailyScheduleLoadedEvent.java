package com.curiousily.ipoli.schedule.events;

import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.schedule.DailySchedule;

import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/16/15.
 */
public class DailyScheduleLoadedEvent {

    public final DailySchedule schedule;

    public DailyScheduleLoadedEvent(DailySchedule schedule) {
        this.schedule = schedule;
    }
}
