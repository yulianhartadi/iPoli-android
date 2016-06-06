package io.ipoli.android.quest.data;

import java.util.Date;

import io.ipoli.android.app.net.RemoteObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IDGenerator;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/6/16.
 */
public class RepeatingQuestSchedule extends RealmObject implements RemoteObject<RepeatingQuestSchedule> {

    public enum ScheduleType {DAILY, WEEKLY, MONTHLY, YEARLY}

    @Required
    @PrimaryKey
    private String id;

    private Date startDate;
    private Date endDate;

    private String type;

    private RepeatingQuest repeatingQuest;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    private Boolean needsSyncWithRemote;
    private String remoteId;

    public RepeatingQuestSchedule() {
    }

    public RepeatingQuestSchedule(Date startDate, Date endDate, RepeatingQuest repeatingQuest, ScheduleType scheduleType) {
        this.id = IDGenerator.generate();
        this.startDate = startDate;
        this.endDate = endDate;
        this.repeatingQuest = repeatingQuest;
        this.type = scheduleType.name();
        createdAt = DateUtils.nowUTC();
        updatedAt = DateUtils.nowUTC();
        needsSyncWithRemote = true;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RepeatingQuest getRepeatingQuest() {
        return repeatingQuest;
    }

    public void setRepeatingQuest(RepeatingQuest repeatingQuest) {
        this.repeatingQuest = repeatingQuest;
    }

    @Override
    public void markUpdated() {
        setNeedsSync();
        setUpdatedAt(DateUtils.nowUTC());
    }

    @Override
    public void setNeedsSync() {
        needsSyncWithRemote = true;
    }

    @Override
    public boolean needsSyncWithRemote() {
        return needsSyncWithRemote;
    }

    @Override
    public void setSyncedWithRemote() {
        needsSyncWithRemote = false;
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

    @Override
    public String getRemoteId() {
        return remoteId;
    }

    @Override
    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }
}