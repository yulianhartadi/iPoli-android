package io.ipoli.android.quest.viewmodels;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;

import java.text.ParseException;

import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import io.ipoli.android.quest.ui.formatters.FrequencyTextFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/9/16.
 */
public class RepeatingQuestViewModel {

    private final RepeatingQuest repeatingQuest;

    public RepeatingQuestViewModel(RepeatingQuest repeatingQuest) {
        this.repeatingQuest = repeatingQuest;
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

    private Category getQuestCategory() {
        return RepeatingQuest.getCategory(repeatingQuest);
    }

    public RepeatingQuest getRepeatingQuest() {
        return repeatingQuest;
    }

    public String getScheduleText() {
        String txt = FrequencyTextFormatter.formatInterval(getFrequency(), repeatingQuest.getRecurrence());
        int duration = repeatingQuest.getDuration();
        Time startTime = RepeatingQuest.getStartTime(repeatingQuest);
        if (duration > 0 && startTime != null) {
            Time endTime = Time.plusMinutes(startTime, duration);
            txt += " at " + startTime + " - " + endTime;
        } else {
            txt += " for " + DurationFormatter.formatReadable(duration);
        }
        return txt;
    }

    private int getFrequency() {
        Recurrence recurrence = repeatingQuest.getRecurrence();
        if (recurrence.isFlexible()) {
            return recurrence.getFlexibleCount();
        }
        if (recurrence.getRecurrenceType() == Recurrence.RecurrenceType.DAILY) {
            return 7;
        }
        if (recurrence.getRecurrenceType() == Recurrence.RecurrenceType.MONTHLY) {
            return 1;
        }
        try {
            Recur recur = new Recur(recurrence.getRrule());
            return recur.getDayList().size();
        } catch (ParseException e) {
            return 0;
        }
    }

}