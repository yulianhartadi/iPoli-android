package io.ipoli.android.quest.viewmodels;

import android.support.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.app.scheduling.TimeSlot;
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
    private List<TimeSlot> proposedSlots;
    private Category category;

    public QuestCalendarViewModel(Quest quest) {
        this.quest = quest;
        this.name = quest.getName();
        this.backgroundColor = quest.getCategoryType().color50;
        this.dragBackgroundColor = quest.getCategoryType().color500;
        this.startMinute = quest.getActualStartMinute();
        this.shouldDisplayAsProposedSlot = false;
        this.proposedSlots = new ArrayList<>();
        this.category = quest.getCategoryType();
    }

    public static QuestCalendarViewModel createWithProposedTime(Quest quest, int startMinute, List<TimeSlot> proposedSlots) {
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

    public boolean useNextSlot(List<QuestCalendarViewModel> eventsWithProposedSlots) {
        TimeSlot timeSlot = null;
        for (TimeSlot tb : proposedSlots) {
            if (!doOverlap(eventsWithProposedSlots, tb)) {
                timeSlot = tb;
                break;
            }
        }
        if (timeSlot != null) {
            setStartMinute(timeSlot.getStartMinute());
            proposedSlots.remove(timeSlot);
            return true;
        }
        return false;
    }

    private boolean doOverlap(List<QuestCalendarViewModel> eventsWithProposedSlots, TimeSlot tb) {
        for (QuestCalendarViewModel vm : eventsWithProposedSlots) {
            if (vm != this) {
                if (tb.doOverlap(vm.getStartMinute(), vm.getStartMinute() + vm.getDuration() - 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getPriority() {
        return quest.getPriority();
    }

    public TimePreference getStartTimePreference() {
        return quest.getStartTimePreference();
    }
}
