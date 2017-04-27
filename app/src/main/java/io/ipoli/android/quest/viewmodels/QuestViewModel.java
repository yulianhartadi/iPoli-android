package io.ipoli.android.quest.viewmodels;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import org.threeten.bp.LocalDate;

import java.util.Locale;

import io.ipoli.android.app.ui.formatters.DateFormatter;
import io.ipoli.android.app.ui.formatters.DurationFormatter;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/10/16.
 */
public class QuestViewModel {

    private final Context context;
    private final Quest quest;
    private final int timesADay;
    private final int remainingCount;
    private final boolean use24HourFormat;

    public QuestViewModel(Context context, Quest quest, boolean use24HourFormat) {
        this.context = context;
        this.quest = quest;
        this.timesADay = quest.getTimesADay();
        this.remainingCount = quest.getRemainingCount();
        this.use24HourFormat = use24HourFormat;
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
        return getQuestCategory().colorfulImage;
    }

    public String getDueDateText(LocalDate currentDate) {
        return DateFormatter.formatWithoutYear(quest.getScheduledDate(), currentDate);
    }

    private Category getQuestCategory() {
        return quest.getCategoryType();
    }

    public Quest getQuest() {
        return quest;
    }

    public String getScheduleText() {
        int duration = quest.getDuration();
        Time startTime = quest.getStartTime();
        if (duration > 0 && startTime != null) {
            Time endTime = Time.plusMinutes(startTime, duration);
            return startTime.toString(use24HourFormat) + " - " + endTime.toString(use24HourFormat);
        } else if (duration > 0) {
            return "for " + DurationFormatter.format(context, duration);
        } else if (startTime != null) {
            return "at " + startTime.toString(use24HourFormat);
        }
        return "";
    }

    public int getCompletedCount() {
        return timesADay - remainingCount;
    }

    public int getRemainingCount() {
        return remainingCount;
    }

    public boolean isRecurrent() {
        return quest.isFromRepeatingQuest();
    }

    public boolean hasTimesADay() {
        return timesADay > 1;
    }

    public String getRemainingText() {
        if (timesADay == 1) {
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
        return quest.isFromChallenge();
    }

    public boolean isCompleted() {
        return quest.isCompleted();
    }

    public Integer getCompletedAtMinute() {
        return quest.getCompletedAtMinute();
    }
}
