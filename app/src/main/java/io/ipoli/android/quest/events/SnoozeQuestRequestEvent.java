package io.ipoli.android.quest.events;

import java.util.Date;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/14/16.
 */
public class SnoozeQuestRequestEvent {
    public final Quest quest;
    public final int minutes;
    public final Date date;
    public final boolean showTimePicker;
    public final boolean showDatePicker;

    public SnoozeQuestRequestEvent(Quest quest, int minutes, Date date, boolean showTimePicker, boolean showDatePicker) {
        this.quest = quest;
        this.minutes = minutes;
        this.date = date;
        this.showTimePicker = showTimePicker;
        this.showDatePicker = showDatePicker;
    }
}
