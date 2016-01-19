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

    public NewTodayQuestEvent(String name, Date startTime, Integer duration) {
        this.name = name;
        this.startTime = startTime;
        this.duration = duration;
    }
}
