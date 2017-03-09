package io.ipoli.android.reminder.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/26/16.
 */
public class Reminder {

    private String message;

    private Long minutesFromStart;

    private Integer notificationId;

    private Long start;

    public Reminder() {
    }

    public Reminder(long minutesFromStart, int notificationId) {
        this.notificationId = notificationId;
        this.minutesFromStart = minutesFromStart;
    }

    public long getMinutesFromStart() {
        return minutesFromStart;
    }

    public void setMinutesFromStart(long minutesFromStart) {
        this.minutesFromStart = minutesFromStart;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Integer getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    @JsonIgnore
    public void calculateStartTime(Quest quest) {
        Date questStartTime = Quest.getStartDateTime(quest);
        if (questStartTime == null) {
            start = null;
            return;
        }
        start = questStartTime.getTime() + TimeUnit.MINUTES.toMillis(getMinutesFromStart());
    }

    @JsonIgnore
    public Date getStartTime() {
        return start != null ? new Date(start) : null;
    }

    @JsonIgnore
    public void setStartTime(Date startTime) {
        start = startTime != null ? startTime.getTime() : null;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }
}
