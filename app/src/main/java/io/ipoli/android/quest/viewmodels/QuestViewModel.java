package io.ipoli.android.quest.viewmodels;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import java.util.Locale;

import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.ui.formatters.DueDateFormatter;
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
    public int getContextColor() {
        return getQuestContext().resLightColor;
    }

    @DrawableRes
    public int getContextImage() {
        return getQuestContext().whiteImage;
    }

    public String getDueDateText() {
        return DueDateFormatter.formatWithoutYear(quest.getEndDate());
    }

    private QuestContext getQuestContext() {
        return Quest.getContext(quest);
    }

    public Quest getQuest() {
        return quest;
    }

    public String getScheduleText() {
        int duration = quest.getDuration();
        Time startTime = Quest.getStartTime(quest);
        if (duration > 0 && startTime != null) {
            Time endTime = Time.addMinutes(startTime, duration);
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
}
