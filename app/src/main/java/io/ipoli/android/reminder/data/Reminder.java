package io.ipoli.android.reminder.data;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/26/16.
 */
public class Reminder extends PersistedObject {

    private String message;

    private Long minutesFromStart;

    private Integer notificationId;

    private Long start;

    public Reminder() {
    }

    public Reminder(long minutesFromStart, int notificationId) {
        this.notificationId = notificationId;
        this.minutesFromStart = minutesFromStart;
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
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

    public void calculateStartTime(Quest quest) {
        Date questStartTime = Quest.getStartDateTime(quest);
        if (questStartTime == null) {
            start = null;
            return;
        }
        start = questStartTime.getTime() + TimeUnit.MINUTES.toMillis(getMinutesFromStart());
    }

    @Exclude
    public Date getStartTime() {
        return start != null ? new Date(start) : null;
    }

    @Exclude
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
