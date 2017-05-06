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
        this(quest.getId(), duration, priority, startTimePreference, category);
        this.quest = quest;
    }

    public QuestTask(String id, int startMinute, int duration, int priority, TimePreference startTimePreference, Category category) {
        super(id, startMinute, duration, priority, startTimePreference, category);
    }

    public QuestTask(String id, int duration, int priority, TimePreference startTimePreference, Category category) {
        super(id, duration, priority, startTimePreference, category);
    }
}
