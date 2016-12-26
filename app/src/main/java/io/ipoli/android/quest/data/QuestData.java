package io.ipoli.android.quest.data;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/27/16.
 */
public class QuestData {
    private boolean isComplete;
    private Integer duration;

    public QuestData() {

    }

    public QuestData(Quest quest) {
        isComplete = Quest.isCompleted(quest);
        duration = quest.getActualDuration();
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
}
