package io.ipoli.android.quest.viewmodels;

import android.support.annotation.ColorRes;

import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/11/16.
 */
public class UnscheduledQuestViewModel {
    private final Quest quest;

    public UnscheduledQuestViewModel(Quest quest) {
        this.quest = quest;
    }

    public Quest getQuest() {
        return quest;
    }

    public boolean isStarted() {
        return Quest.isStarted(quest);
    }

    @ColorRes
    public int getCategoryColor() {
        return getQuestCategory().color500;
    }

    private Category getQuestCategory() {
        return quest.getCategoryType();
    }

    public String getName() {
        String name = quest.getName();
        if (quest.getRemainingCount() == 1) {
            return name;
        }
        return name + " (x" + quest.getRemainingCount() + ")";
    }

    public boolean isRepeating() {
        return quest.isFromRepeatingQuest();
    }

    public boolean isMostImportant() {
        return quest.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY;
    }

    public boolean isForChallenge() {
        return quest.isFromChallenge();
    }
}
