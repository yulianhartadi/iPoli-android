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
        this(quest.getId(), duration, priority, startTimePreference, category, quest);
    }

    public QuestTask(String id, int duration, int priority, TimePreference startTimePreference, Category category, Quest quest) {
        super(id, duration, priority, startTimePreference, category);
        this.quest = quest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestTask)) return false;
        if (!super.equals(o)) return false;

        QuestTask questTask = (QuestTask) o;

        return areQuestsEqual(quest, questTask.quest);

    }

    private boolean areQuestsEqual(Quest q1, Quest q2) {
        if (q1.getRawText() != null ? !q1.getRawText().equals(q2.getRawText()) : q2.getRawText() != null)
            return false;
        if (!q1.getName().equals(q2.getName())) return false;
        if (!q1.getCategory().equals(q2.getCategory())) return false;
        if (q1.getPriority() != q2.getPriority()) return false;
        if (q1.getStartMinute() != null ? !q1.getStartMinute().equals(q2.getStartMinute()) : q2.getStartMinute() != null)
            return false;
        if (q1.getPreferredStartTime() != null ? !q1.getPreferredStartTime().equals(q2.getPreferredStartTime()) : q2.getPreferredStartTime() != null)
            return false;
        if (q1.getDuration() != q2.getDuration()) return false;
        if (q1.getStart() != null ? !q1.getStart().equals(q2.getStart()) : q2.getStart() != null)
            return false;
        if (q1.getEnd() != null ? !q1.getEnd().equals(q2.getEnd()) : q2.getEnd() != null)
            return false;
        if (q1.getScheduled() != null ? !q1.getScheduled().equals(q2.getScheduled()) : q2.getScheduled() != null)
            return false;
        if (q1.getDifficulty() != null ? !q1.getDifficulty().equals(q2.getDifficulty()) : q2.getDifficulty() != null)
            return false;
        if (q1.getCompletedAt() != null ? !q1.getCompletedAt().equals(q2.getCompletedAt()) : q2.getCompletedAt() != null)
            return false;
        return q1.getCompletedAtMinute() != null ? q1.getCompletedAtMinute().equals(q2.getCompletedAtMinute()) : q2.getCompletedAtMinute() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + quest.hashCode();
        return result;
    }
}
