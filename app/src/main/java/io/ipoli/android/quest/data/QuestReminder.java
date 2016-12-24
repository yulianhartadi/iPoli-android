package io.ipoli.android.quest.data;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/24/16.
 */

public class QuestReminder {

    private Long minutesFromStart;
    private String questId;
    private String questName;
    private Long start;
    private Integer notificationId;

    public Long getMinutesFromStart() {
        return minutesFromStart;
    }

    public void setMinutesFromStart(Long minutesFromStart) {
        this.minutesFromStart = minutesFromStart;
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }

    public String getQuestName() {
        return questName;
    }

    public void setQuestName(String questName) {
        this.questName = questName;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long startTime) {
        this.start = startTime;
    }

    public Integer getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }
}
