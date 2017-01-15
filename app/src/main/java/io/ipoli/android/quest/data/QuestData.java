package io.ipoli.android.quest.data;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/27/16.
 */
public class QuestData {
    private boolean isComplete;
    private Integer duration;
    private Long scheduledDate;
    private Long originalScheduledDate;

    public QuestData() {

    }

    public QuestData(Quest quest) {
        isComplete = quest.isCompleted();
        duration = quest.getActualDuration();
        scheduledDate = quest.getScheduled();
        originalScheduledDate = quest.getOriginalScheduled();
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

    public Long getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(Long scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public void setOriginalScheduledDate(Long originalScheduledDate) {
        this.originalScheduledDate = originalScheduledDate;
    }

    public Long getOriginalScheduledDate() {
        return originalScheduledDate;
    }
}
