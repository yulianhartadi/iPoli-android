package io.ipoli.android.quest.viewmodels;

import android.support.annotation.ColorRes;

import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/11/16.
 */
public class UnscheduledQuestViewModel {
    private final Quest quest;
    private int remainingCount;

    public UnscheduledQuestViewModel(Quest quest, int remainingCount) {
        this.quest = quest;
        this.remainingCount = remainingCount;
    }

    public Quest getQuest() {
        return quest;
    }

    public boolean isStarted() {
        return Quest.isStarted(quest);
    }

    @ColorRes
    public int getContextColor() {
        return getQuestContext().resLightColor;
    }

    private Category getQuestContext() {
        return Quest.getCategory(quest);
    }

    public String getName() {
        String name = quest.getName();
        if (remainingCount == 1) {
            return name;
        }
        return name + " (x" + remainingCount + ")";
    }

    public int getRemainingCount() {
        return remainingCount;
    }

    public void decreaseRemainingCount() {
        remainingCount--;
    }
}
