package io.ipoli.android.quest.viewmodels;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import java.util.Locale;

import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.ui.formatters.DateFormatter;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/10/16.
 */
public class QuestViewModel {

    private final Context context;
    private final Quest quest;
    private final int repeatCount;
    private final int remainingCount;

    public QuestViewModel(Context context, Quest quest, int repeatCount, int remainingCount) {
        this.context = context;
        this.quest = quest;
        this.repeatCount = repeatCount;
        this.remainingCount = remainingCount;
    }

    public String getName() {
        return quest.getName();
    }

    @ColorRes
    public int getCategoryColor() {
        return getQuestCategory().color500;
    }

    @DrawableRes
    public int getCategoryImage() {
        return getQuestCategory().whiteImage;
    }

    public String getDueDateText() {
        return DateFormatter.formatWithoutYear(quest.getEndDate());
    }

    private Category getQuestCategory() {
        return quest.getCategory();
    }

    public Quest getQuest() {
        return quest;
    }

    public String getScheduleText() {
        int duration = quest.getDuration();
        Time startTime = Quest.getStartTime(quest);
        if (duration > 0 && startTime != null) {
            Time endTime = Time.plusMinutes(startTime, duration);
            return startTime + " - " + endTime;
        } else if (duration > 0) {
            return "for " + DurationFormatter.format(context, duration);
        } else if (startTime != null) {
            return "at " + startTime;
        }
        return "";
    }

    public int getCompletedCount() {
        return repeatCount - remainingCount;
    }

    public int getRemainingCount() {
        return remainingCount;
    }

    public boolean isRecurrent() {
        return quest.getRepeatingQuest() != null;
    }

    public boolean hasTimesADay() {
        return repeatCount > 1;
    }

    public String getRemainingText() {
        if (repeatCount == 1) {
            return "";
        }
        return String.format(Locale.getDefault(), "x%d more", remainingCount);
    }

    public boolean isStarted() {
        return Quest.isStarted(quest);
    }

    public boolean isMostImportant() {
        return quest.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY;
    }

    public boolean isForChallenge() {
        return quest.getChallengeId() != null;
    }
}
