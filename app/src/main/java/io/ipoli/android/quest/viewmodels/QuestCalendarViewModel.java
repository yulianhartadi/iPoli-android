package io.ipoli.android.quest.viewmodels;

import android.support.annotation.DrawableRes;

import io.ipoli.android.app.ui.calendar.CalendarEvent;
import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class QuestCalendarViewModel implements CalendarEvent {

    private final String name;
    private final int backgroundColor;
    private final int dragBackgroundColor;
    private final Quest quest;
    private boolean shouldDisplayAsProposedSlot;
    private Integer startMinute;
    private Category category;

    public QuestCalendarViewModel(Quest quest) {
        this.quest = quest;
        this.name = quest.getName();
        this.backgroundColor = quest.getCategoryType().color50;
        this.dragBackgroundColor = quest.getCategoryType().color500;
        this.startMinute = quest.getActualStartMinute();
        this.shouldDisplayAsProposedSlot = false;
        this.category = quest.getCategoryType();
    }

    public static QuestCalendarViewModel createWithProposedTime(Quest quest, int startMinute) {
        QuestCalendarViewModel vm = new QuestCalendarViewModel(quest);
        vm.setStartMinute(startMinute);
        vm.shouldDisplayAsProposedSlot = true;
        return vm;
    }

    @Override
    public Integer getStartMinute() {
        return startMinute;
    }

    @Override
    public int getDuration() {
        return quest.getActualDuration();
    }

    @Override
    public void setStartMinute(Integer startMinute) {
        this.startMinute = startMinute;
    }

    @Override
    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public int getDragBackgroundColor() {
        return dragBackgroundColor;
    }

    @Override
    public String getName() {
        return name;
    }

    public Quest getQuest() {
        return quest;
    }

    public String getId() {
        return quest.getId();
    }

    @Override
    public boolean isRepeating() {
        return quest.isFromRepeatingQuest();
    }

    @DrawableRes
    public int getContextImage() {
        return quest.getCategoryType().colorfulImage;
    }

    @Override
    public boolean isMostImportant() {
        return quest.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY;
    }

    @Override
    public boolean isForChallenge() {
        return quest.isFromChallenge();
    }

    public Category getCategory() {
        return category;
    }

    public boolean shouldDisplayAsProposedSlot() {
        return shouldDisplayAsProposedSlot;
    }

    public int getPriority() {
        return quest.getPriority();
    }

    public TimePreference getStartTimePreference() {
        return quest.getStartTimePreference();
    }
}
