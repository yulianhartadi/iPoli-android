package io.ipoli.android.quest.data;

import java.util.Date;

import io.ipoli.android.app.net.RemoteObject;
import io.ipoli.android.app.utils.DateUtils;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class SourceMapping extends RealmObject implements RemoteObject<SourceMapping> {

    @Required
    @PrimaryKey
    private String id;

    private String androidCalendar;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    private boolean needsSyncWithRemote;
    private boolean isRemoteObject;

    public SourceMapping() {
    }

    public static SourceMapping fromGoogleCalendar(long eventId) {
        SourceMapping sourceMapping = new SourceMapping();
        sourceMapping.createdAt = DateUtils.nowUTC();
        sourceMapping.updatedAt = DateUtils.nowUTC();
        sourceMapping.needsSyncWithRemote = true;
        sourceMapping.isRemoteObject = false;
        sourceMapping.androidCalendar = String.valueOf(eventId);
        return sourceMapping;
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

    public String getAndroidCalendar() {
        return androidCalendar;
    }

    public void setAndroidCalendar(long googleCalendar) {
        this.androidCalendar = String.valueOf(googleCalendar);
    }
}
