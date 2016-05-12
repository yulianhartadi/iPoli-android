package io.ipoli.android.quest.data;

import java.util.Date;
import java.util.UUID;

import io.ipoli.android.app.net.RemoteObject;
import io.ipoli.android.app.utils.DateUtils;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class ExternalSourceMapping extends RealmObject implements RemoteObject<ExternalSourceMapping> {

    @Required
    @PrimaryKey
    private String id;

    private String googleCalendar;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    private boolean needsSyncWithRemote;
    private boolean isRemoteObject;

    public ExternalSourceMapping() {
    }

    public ExternalSourceMapping(String googleCalendar) {
        this.id = UUID.randomUUID().toString();
        this.googleCalendar = googleCalendar;
        this.createdAt = DateUtils.nowUTC();
        this.updatedAt = DateUtils.nowUTC();
        this.needsSyncWithRemote = true;
        this.isRemoteObject = false;
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

    @Override
    public void setRemoteObject() {
        isRemoteObject = true;
    }

    @Override
    public boolean isRemoteObject() {
        return isRemoteObject;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getGoogleCalendar() {
        return googleCalendar;
    }

    public void setGoogleCalendar(String googleCalendar) {
        this.googleCalendar = googleCalendar;
    }
}
