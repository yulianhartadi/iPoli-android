package io.ipoli.android.quest.viewmodels;

import android.content.Context;
import android.support.annotation.DrawableRes;

import io.ipoli.android.app.ui.formatters.DurationFormatter;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/10/16.
 */
public class AgendaViewModel {

    private final Context context;
    private final Quest quest;
    private final boolean use24HourFormat;

    public AgendaViewModel(Context context, Quest quest, boolean use24HourFormat) {
        this.context = context;
        this.quest = quest;
        this.use24HourFormat = use24HourFormat;
    }

    public String getName() {
        return quest.getName();
    }

    @DrawableRes
    public int getCategoryImage() {
        return quest.getCategoryType().colorfulImage;
    }

    public Quest getQuest() {
        return quest;
    }

    public String getScheduleText() {
        int duration = quest.getDuration();
        Time startTime = quest.getStartTime();
        if (duration > 0 && startTime != null) {
            Time endTime = Time.plusMinutes(startTime, duration);
            return startTime.toString(use24HourFormat) + "\n" + endTime.toString(use24HourFormat);
        } else if (duration > 0) {
            return "for\n" + DurationFormatter.format(context, duration);
        } else if (startTime != null) {
            return "at\n" + startTime.toString(use24HourFormat);
        }
        return "";
    }

    public boolean isCompleted() {
        return quest.isCompleted();
    }
}
