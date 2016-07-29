package io.ipoli.android.quest.data;

import java.util.Date;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class SourceMapping extends PersistedObject {

    private String androidCalendar;

    public SourceMapping() {
    }

    public static SourceMapping fromGoogleCalendar(long eventId) {
        SourceMapping sourceMapping = new SourceMapping();
        sourceMapping.createdAt = DateUtils.nowUTC();
        sourceMapping.updatedAt = DateUtils.nowUTC();
        sourceMapping.androidCalendar = String.valueOf(eventId);
        return sourceMapping;
    }

    public Date getUpdatedAt() {
        return updatedAt;
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
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getAndroidCalendar() {
        return androidCalendar;
    }

    public void setAndroidCalendar(long googleCalendar) {
        this.androidCalendar = String.valueOf(googleCalendar);
    }
}
