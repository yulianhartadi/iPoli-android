package io.ipoli.android.quest.viewmodels;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Date;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.parameter.Value;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.RecurrentQuest;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/9/16.
 */
public class RecurrentQuestViewModel {

    private final RecurrentQuest recurrentQuest;
    private final int completedCount;
    private final Recur recur;
    private final java.util.Date nextDate;

    public RecurrentQuestViewModel(RecurrentQuest recurrentQuest, int completedCount, Recur recur, java.util.Date nextDate) {
        this.recurrentQuest = recurrentQuest;
        this.completedCount = completedCount;
        this.recur = recur;
        this.nextDate = nextDate;
    }

    public String getName() {
        return recurrentQuest.getName();
    }

    @ColorRes
    public int getContextColor() {
        return getQuestContext().resLightColor;
    }

    @DrawableRes
    public int getContextImage() {
        return getQuestContext().whiteImage;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public int getRemainingCount() {
        return getTotalCount() - completedCount;
    }

    private QuestContext getQuestContext() {
        return RecurrentQuest.getContext(recurrentQuest);
    }

    public String getNextText() {
        String nextText = "";
        if(recur == null || nextDate == null) {
            nextText += "Unscheduled";
        } else {
            if (DateUtils.isToday(nextDate)) {
                nextText = "Today";
            } else if (DateUtils.isTomorrow(nextDate)) {
                nextText = "Tomorrow";
            } else {
                nextText = new SimpleDateFormat("dd MMM", Locale.getDefault()).format(nextDate);
            }
        }

        nextText += " ";

        int duration = recurrentQuest.getDuration();
        Time startTime = RecurrentQuest.getStartTime(recurrentQuest);
        if (duration > 0 && startTime != null) {
            Time endTime = Time.addMinutes(startTime, duration);
            nextText += startTime + " - " + endTime;
        } else if (duration > 0) {
            nextText += "for " + DurationFormatter.formatReadable(duration);
        } else if (startTime != null){
            nextText += startTime;
        }
        return "Next: " + nextText;
    }

    public int getTotalCount() {
        if(recur == null) {
            return -1;
        }
        if(recur.getFrequency().equals(Recur.MONTHLY)) {
            return 1;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(DateUtils.getLastDateOfWeek());
        c.add(Calendar.DAY_OF_YEAR, 1);

        Calendar c1 = DateUtils.getTodayAtMidnight();
        Calendar c2 = Calendar.getInstance();
        c2.setTime(recurrentQuest.getRecurrence().getDtstart());
        c1.set(Calendar.DAY_OF_YEAR, c2.get(Calendar.DAY_OF_YEAR));
        c1.set(Calendar.YEAR, c2.get(Calendar.YEAR));
        return recur.getDates(new Date(c1.getTime()), new Date(DateUtils.getFirstDateOfWeek()), new Date(c.getTime()), Value.DATE).size();
    }

    public String getRepeatText() {
        if(recur == null) {
            return "";
        }

        int remainingCount = getRemainingCount();

        if (remainingCount <= 0) {
            return "Done";
        }

        if(recur.getFrequency().equals(Recur.MONTHLY)) {
            return remainingCount + " more this month";
        }

        return remainingCount + " more this week";

    }

    public RecurrentQuest getRecurrentQuest() {
        return recurrentQuest;
    }
}