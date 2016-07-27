package io.ipoli.android.quest.data;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IDGenerator;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/26/16.
 */
public class Reminder {

    private String id;

    private String message;

    private Long minutesFromStart;

    @Exclude
    private Integer notificationId;

    private Date startTime;

    private Date createdAt;

    private Date updatedAt;

    private Boolean isDeleted;

    public Reminder() {
    }

    public Reminder(long minutesFromStart, int notificationId) {
        this.id = IDGenerator.generate();
        this.notificationId = notificationId;
        this.minutesFromStart = minutesFromStart;
        this.isDeleted = false;
        createdAt = DateUtils.nowUTC();
        updatedAt = DateUtils.nowUTC();
    }

    @Exclude
    public String getId() {
        return id;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void markDeleted() {
        isDeleted = true;
        setUpdatedAt(DateUtils.nowUTC());
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
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
