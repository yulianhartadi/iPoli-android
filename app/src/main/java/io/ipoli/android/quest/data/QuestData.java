package io.ipoli.android.quest.data;

import org.threeten.bp.LocalDate;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/27/16.
 */
public class QuestData {
    private boolean isComplete;
    private Integer duration;
    private LocalDate scheduledDate;
    private LocalDate originalScheduledDate;

    public QuestData() {

    }

    public QuestData(Quest quest) {
        isComplete = quest.isCompleted();
        duration = quest.getActualDuration();
        scheduledDate = quest.getScheduledDate();
        originalScheduledDate = quest.getOriginalScheduledDate();
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public LocalDate getOriginalScheduledDate() {
        return originalScheduledDate;
    }
}
