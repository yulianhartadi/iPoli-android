package io.ipoli.android.reminder.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/26/16.
 */
public class Reminder {

    private String message;

    private Long minutesFromStart;

    private String notificationId;

    private Long start;

    public Reminder() {
    }

    public Reminder(long minutesFromStart) {
        this(minutesFromStart, String.valueOf(new Random().nextInt()));
    }

    public Reminder(long minutesFromStart, String notificationId) {
        this.minutesFromStart = minutesFromStart;
        this.notificationId = notificationId;
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

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    @JsonIgnore
    public int getNotificationNum() {
        return Integer.valueOf(notificationId);
    }

    @JsonIgnore
    public void calculateStartTime(Quest quest) {
        Long questStartTime = Quest.getStartDateTimeMillis(quest);
        if (questStartTime == null) {
            start = null;
            return;
        }
        start = questStartTime + TimeUnit.MINUTES.toMillis(getMinutesFromStart());
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
