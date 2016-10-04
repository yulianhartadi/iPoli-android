package io.ipoli.android.quest.viewmodels;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Locale;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/9/16.
 */
public class RepeatingQuestViewModel {

    private final RepeatingQuest repeatingQuest;
    private long totalCount;
    private int completedCount;
    private java.util.Date nextDate;
    private int timesADay;

    public RepeatingQuestViewModel(RepeatingQuest repeatingQuest) {
        this.repeatingQuest = repeatingQuest;
        this.totalCount = -1;
        this.completedCount = -1;
        this.nextDate = null;
        this.timesADay = repeatingQuest.getRecurrence().getTimesADay();
    }

    public RepeatingQuestViewModel(RepeatingQuest repeatingQuest, long totalCount, int completedCount, java.util.Date nextDate) {
        this.repeatingQuest = repeatingQuest;
        this.totalCount = totalCount;
        this.completedCount = completedCount;
        this.nextDate = nextDate;
        this.timesADay = repeatingQuest.getRecurrence().getTimesADay();
    }

    public String getName() {
        return repeatingQuest.getName();
    }

    @ColorRes
    public int getCategoryColor() {
        return getQuestCategory().color500;
    }

    @DrawableRes
    public int getCategoryImage() {
        return getQuestCategory().whiteImage;
    }

    public int getCompletedDailyCount() {
        return (int) Math.floor((double) completedCount / (double) timesADay);
    }

    public int getRemainingDailyCount() {
        return (int) Math.ceil((double) (totalCount - completedCount) / (double) timesADay);
    }

    private Category getQuestCategory() {
        return RepeatingQuest.getCategory(repeatingQuest);
    }

    public String getNextText() {
        String nextText = "";
        if (nextDate == null) {
            nextText += "Unscheduled";
        } else {
            if (DateUtils.isTodayUTC(nextDate)) {
                nextText = "Today";
            } else if (DateUtils.isTomorrowUTC(nextDate)) {
                nextText = "Tomorrow";
            } else {
                nextText = new SimpleDateFormat("dd MMM", Locale.getDefault()).format(new LocalDate(nextDate, DateTimeZone.UTC).toDate());
            }
        }

        nextText += " ";

        int duration = repeatingQuest.getDuration();
        Time startTime = RepeatingQuest.getStartTime(repeatingQuest);
        if (duration > 0 && startTime != null) {
            Time endTime = Time.plusMinutes(startTime, duration);
            nextText += startTime + " - " + endTime;
        } else if (duration > 0) {
            nextText += "for " + DurationFormatter.formatReadable(duration);
        } else if (startTime != null) {
            nextText += startTime;
        }
        return "Next: " + nextText;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public String getRepeatText() {

        int remainingCount = getRemainingDailyCount();

        if (remainingCount <= 0) {
            return "Done";
        }

        Recurrence recurrence = repeatingQuest.getRecurrence();
        if (recurrence.getRecurrenceType() == Recurrence.RecurrenceType.MONTHLY && !repeatingQuest.isFlexible()) {
            return remainingCount + " more this month";
        }

        return remainingCount + " more this week";

    }

    public RepeatingQuest getRepeatingQuest() {
        return repeatingQuest;
    }

    public boolean isLoaded() {
        return totalCount >= 0;
    }

}