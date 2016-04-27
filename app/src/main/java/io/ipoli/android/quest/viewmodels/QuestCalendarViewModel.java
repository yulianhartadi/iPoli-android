package io.ipoli.android.quest.viewmodels;

import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.app.scheduling.dto.Slot;
import io.ipoli.android.app.ui.calendar.CalendarEvent;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class QuestCalendarViewModel implements CalendarEvent {

    private static final int EMPIRICALLY_TESTED_MINUTES_FOR_INDICATOR = 6;

    private final String name;
    private final List<Slot> suggestedSlots;
    private int currentSlot;
    private int duration;
    private final int backgroundColor;
    private final Quest quest;
    private int startTime;

    public QuestCalendarViewModel(Quest quest) {
        this(quest, new ArrayList<>());
    }

    public QuestCalendarViewModel(Quest quest, List<Slot> suggestedSlots) {
        this.quest = quest;
        this.suggestedSlots = suggestedSlots;
        this.currentSlot = 0;
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

    @Override
    public boolean shouldDisplayAsIndicator() {
        return quest.isIndicator();
    }

    @DrawableRes
    public int getContextImage() {
        return Quest.getContext(quest).colorfulImage;
    }

    @Override
    public boolean shouldDisplayAsSuggestion() {
        return !suggestedSlots.isEmpty();
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Slot nextSlot() {
        currentSlot++;
        if (currentSlot >= suggestedSlots.size()) {
            currentSlot = 0;
        }
        return suggestedSlots.get(currentSlot);
    }
}
