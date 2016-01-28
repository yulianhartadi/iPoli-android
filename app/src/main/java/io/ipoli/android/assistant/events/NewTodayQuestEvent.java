package io.ipoli.android.assistant.events;

import java.util.Date;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/11/16.
 */
public class NewTodayQuestEvent {
    public final String name;
    public final Date startTime;
    public final Integer duration;
    public final Date dueDate;

    public NewTodayQuestEvent(String name, Date startTime, Integer duration, Date dueDate) {
        this.name = name;
        this.startTime = startTime;
        this.duration = duration;
        this.dueDate = dueDate;
    }
}
