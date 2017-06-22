package io.ipoli.android.quest.viewmodels;

import android.support.annotation.ColorRes;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;

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

    public SpannableString getName() {
        if (isCompleted()) {
            SpannableString spannableString = new SpannableString(quest.getName());
            spannableString.setSpan(new StrikethroughSpan(), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            return spannableString;
        }
        if(quest.getTimesADay() > 1 ) {
            return new SpannableString(quest.getName() + " (x" + quest.getRemainingCount() + ")");
        }
        return new SpannableString(quest.getName());
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

    public boolean isCompleted() {
        return quest.isCompleted();
    }
}
