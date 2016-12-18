package io.ipoli.android.quest.viewmodels;

import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.app.scheduling.TimeBlock;
import io.ipoli.android.app.ui.calendar.CalendarEvent;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class QuestCalendarViewModel implements CalendarEvent {

    private final String name;
    private final int backgroundColor;
    private final Quest quest;
    private boolean shouldDisplayAsProposedSlot;
    private int startMinute;
    private List<TimeBlock> proposedSlots;

    public QuestCalendarViewModel(Quest quest) {
        this.quest = quest;
        this.name = quest.getName();
        this.backgroundColor = Quest.getCategory(quest).color50;
        this.startMinute = quest.getActualStartMinute();
        this.shouldDisplayAsProposedSlot = false;
        this.proposedSlots = new ArrayList<>();
    }

    public static QuestCalendarViewModel createWithProposedTime(Quest quest, int startMinute, List<TimeBlock> proposedSlots) {
        QuestCalendarViewModel vm = new QuestCalendarViewModel(quest);
        vm.setStartMinute(startMinute);
        vm.shouldDisplayAsProposedSlot = true;
        vm.proposedSlots = proposedSlots;
        return vm;
    }

    @Override
    public int getStartMinute() {
        return startMinute;
    }

    @Override
    public int getDuration() {
        return quest.getActualDuration();
    }

    @Override
    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
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
        return Quest.getCategory(quest).colorfulImage;
    }

    @Override
    public boolean isMostImportant() {
        return quest.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY;
    }

    @Override
    public boolean isForChallenge() {
        return quest.getChallengeId() != null;
    }

    public boolean shouldDisplayAsProposedSlot() {
        return shouldDisplayAsProposedSlot;
    }

    public void useNextSlot() {
        if (!proposedSlots.isEmpty()) {
            proposedSlots.remove(0);
        }
        if (!proposedSlots.isEmpty()) {
            setStartMinute(proposedSlots.get(0).getStartMinute());
        }
    }
}
