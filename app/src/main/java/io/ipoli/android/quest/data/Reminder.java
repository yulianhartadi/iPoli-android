package io.ipoli.android.quest.data;

import java.util.Date;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IDGenerator;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/26/16.
 */
public class Reminder extends RealmObject {

    @Required
    @PrimaryKey
    private String id;

    private String message;

    @Required
    private Long minutesFromStart;

    @Required
    private Integer notificationId;

    @Required
    private Integer intentId;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    public Reminder() {
    }

    public Reminder(int minutesFromStart, int notificationId, int intentId) {
        this.id = IDGenerator.generate();
        this.intentId = intentId;
        this.notificationId = notificationId;
        this.minutesFromStart = minutesFromStart;
        createdAt = DateUtils.nowUTC();
        updatedAt = DateUtils.nowUTC();
    }

    public String getId() {
        return id;
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

    public Integer getNotificationId() {
        return notificationId;
    }

    public String getMessage() {
        return message;
    }

    public Integer getIntentId() {
        return intentId;
    }
}
