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

    public AgendaViewModel(Context context, Quest quest) {
        this.context = context;
        this.quest = quest;
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
        Time startTime = Quest.getStartTime(quest);
        if (duration > 0 && startTime != null) {
            Time endTime = Time.plusMinutes(startTime, duration);
            return startTime + "\n" + endTime;
        } else if (duration > 0) {
            return "for\n" + DurationFormatter.format(context, duration);
        } else if (startTime != null) {
            return "at\n" + startTime;
        }
        return "";
    }

    public boolean isCompleted() {
        return quest.isCompleted();
    }
}
