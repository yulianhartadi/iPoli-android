package io.ipoli.android.quest.viewmodels;

import android.support.annotation.ColorRes;
import android.text.TextUtils;

import io.ipoli.android.quest.data.Category;
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
    public int getCategoryColor() {
        return getQuestCategory().color500;
    }

    private Category getQuestCategory() {
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

    public boolean isRepeating() {
        return quest.getRepeatingQuest() != null && !TextUtils.isEmpty(quest.getRepeatingQuest().getRecurrence().getRrule());
    }

    public boolean isMostImportant() {
        return quest.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY;
    }

    public boolean isForChallenge() {
        return quest.getChallengeId() != null;
    }
}
