package io.ipoli.android.quest.ui;

import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import io.ipoli.android.Constants;
import io.ipoli.android.app.ui.calendar.CalendarEvent;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class QuestCalendarViewModel implements CalendarEvent {

    private static final int EMPIRICALLY_TESTED_MINUTES_FOR_INDICATOR = 6;

    private final String name;
    private final int duration;
    private final int backgroundColor;
    private final Quest quest;
    private int startTime;

    public QuestCalendarViewModel(Quest quest) {
        this.quest = quest;
        this.name = quest.getName();
        if (shouldDisplayAsIndicator()) {
            this.duration = EMPIRICALLY_TESTED_MINUTES_FOR_INDICATOR;
        } else {
            this.duration = Math.max(Constants.QUEST_CALENDAR_EVENT_MIN_DURATION, quest.getDuration());
        }
        this.backgroundColor = Quest.getContext(quest).backgroundColor;
        this.startTime = quest.getStartMinute();
    }

    @Override
    public int getStartMinute() {
        return startTime;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public void setStartMinute(int startMinute) {
        this.startTime = startMinute;
    }

    @Override
    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public String getName() {
        return name;
    }

    public Quest getQuest() {
        return quest;
    }

    @Override
    public boolean isRecurrent() {
        return quest.getRecurrentQuest() != null && !TextUtils.isEmpty(quest.getRecurrentQuest().getRecurrence().getRrule());
    }

    public boolean shouldDisplayAsIndicator() {
        boolean hasTimesPerDay = quest.getRecurrentQuest() != null && !TextUtils.isEmpty(quest.getRecurrentQuest().getRecurrence().getDailyRrule());
        boolean hasShortOrNoDuration = quest.getDuration() < 15;
        return hasTimesPerDay && hasShortOrNoDuration;
    }

    @DrawableRes
    public int getContextImage() {
        return Quest.getContext(quest).colorfulImage;
    }
}
