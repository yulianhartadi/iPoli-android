package io.ipoli.android.quest.viewmodels;

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
    private int duration;
    private final int backgroundColor;
    private final Quest quest;
    private int startTime;

    public QuestCalendarViewModel(Quest quest) {
        this.quest = quest;
        this.name = quest.getName();
        if (shouldDisplayAsIndicator()) {
            this.duration = EMPIRICALLY_TESTED_MINUTES_FOR_INDICATOR;
        } else {
            this.duration = Math.max(Constants.CALENDAR_EVENT_MIN_DURATION, quest.getDuration());
        }
        this.backgroundColor = quest.getCategory().backgroundColor;
        this.startTime = quest.getStartMinute();
    }

    @Override
    public int getStartMinute() {
        return startTime;
    }

    @Override
    public int getDuration() {
        return quest.getActualDuration();
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
    public boolean isRepeating() {
        return quest.getRepeatingQuest() != null && !TextUtils.isEmpty(quest.getRepeatingQuest().getRecurrence().getRrule());
    }

    @Override
    public boolean shouldDisplayAsIndicator() {
        return quest.isIndicator();
    }

    @DrawableRes
    public int getContextImage() {
        return quest.getCategory().colorfulImage;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public boolean isMostImportant() {
        return quest.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY;
    }

    @Override
    public boolean isForChallenge() {
        return quest.getChallenge() != null;
    }
}
