package io.ipoli.android.quest.events;

import java.util.Date;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class NewQuestEvent {
    public final String name;
    public final Date startTime;
    public final int duration;
    public final Date dueDate;

    public NewQuestEvent(String name, Date startTime, int duration, Date dueDate) {
        this.name = name;
        this.startTime = startTime;
        this.duration = duration;
        this.dueDate = dueDate;
    }
}
