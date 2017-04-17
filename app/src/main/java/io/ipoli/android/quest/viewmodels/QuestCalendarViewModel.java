package io.ipoli.android.quest.viewmodels;

import android.support.annotation.DrawableRes;

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
    private final int dragBackgroundColor;
    private final Quest quest;
    private boolean shouldDisplayAsProposedSlot;
    private Integer startMinute;
    private List<TimeBlock> proposedSlots;

    public QuestCalendarViewModel(Quest quest) {
        this.quest = quest;
        this.name = quest.getName();
        this.backgroundColor = quest.getCategoryType().color50;
        this.dragBackgroundColor = quest.getCategoryType().color500;
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

    public boolean shouldDisplayAsProposedSlot() {
        return shouldDisplayAsProposedSlot;
    }

    public boolean useNextSlot(List<QuestCalendarViewModel> eventsWithProposedSlots) {
        TimeBlock timeBlock = null;
        for (TimeBlock tb : proposedSlots) {
            if (!doOverlap(eventsWithProposedSlots, tb)) {
                timeBlock = tb;
                break;
            }
        }
        if (timeBlock != null) {
            setStartMinute(timeBlock.getStartMinute());
            proposedSlots.remove(timeBlock);
            return true;
        }
        return false;
    }

    private boolean doOverlap(List<QuestCalendarViewModel> eventsWithProposedSlots, TimeBlock tb) {
        for (QuestCalendarViewModel vm : eventsWithProposedSlots) {
            if (vm != this) {
                if (tb.doOverlap(vm.getStartMinute(), vm.getStartMinute() + vm.getDuration() - 1)) {
                    return true;
                }
            }
        }
        return false;
    }
}
