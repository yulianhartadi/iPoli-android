package io.ipoli.android.quest.data;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/26/16.
 */
public class Reminder extends PersistedObject {

    private String message;

    private Long minutesFromStart;

    @Exclude
    private Integer notificationId;

    private Date startTime;

    public Reminder() {
    }

    public Reminder(long minutesFromStart, int notificationId) {
        this.notificationId = notificationId;
        this.minutesFromStart = minutesFromStart;
        this.isDeleted = false;
        createdAt = DateUtils.nowUTC();
        updatedAt = DateUtils.nowUTC();
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
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
            startTime = null;
            return;
        }
        startTime = new Date(questStartTime.getTime() + TimeUnit.MINUTES.toMillis(getMinutesFromStart()));
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
}
