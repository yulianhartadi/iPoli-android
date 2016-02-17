package io.ipoli.android.quest.ui;

import java.util.Date;

import io.ipoli.android.Constants;
import io.ipoli.android.app.ui.calendar.CalendarEvent;
import io.ipoli.android.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class QuestCalendarEvent implements CalendarEvent {

    private final String name;
    private final int duration;
    private final int backgroundColor;
    private Date startTime;

    public QuestCalendarEvent(Quest quest) {
        this.name = quest.getName();
        this.duration = Math.max(Constants.QUEST_CALENDAR_EVENT_MIN_DURATION, quest.getDuration());
        this.backgroundColor = Quest.getContext(quest).backgroundColor;
        this.startTime = quest.getStartTime();
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Override
    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public String getName() {
        return name;
    }
}
