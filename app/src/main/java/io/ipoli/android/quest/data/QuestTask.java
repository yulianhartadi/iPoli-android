package io.ipoli.android.quest.data;

import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.utils.TimePreference;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/30/17.
 */
public class QuestTask extends Task {

    public Quest quest;

    public QuestTask(int duration, int priority, TimePreference startTimePreference, Category category, Quest quest) {
        this(duration, priority, startTimePreference, category);
        this.quest = quest;
    }

    public QuestTask(int startMinute, int duration, int priority, TimePreference startTimePreference, Category category) {
        super(startMinute, duration, priority, startTimePreference, category);
    }

    public QuestTask(int duration, int priority, TimePreference startTimePreference, Category category) {
        super(duration, priority, startTimePreference, category);
    }
}
