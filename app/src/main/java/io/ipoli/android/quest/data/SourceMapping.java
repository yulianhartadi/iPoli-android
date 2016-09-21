package io.ipoli.android.quest.data;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class SourceMapping extends PersistedObject {

    private String androidCalendar;

    private SourceMapping() {
    }

    public static SourceMapping fromGoogleCalendar(long eventId) {
        SourceMapping sourceMapping = new SourceMapping();
        sourceMapping.setCreatedAt(DateUtils.nowUTC().getTime());
        sourceMapping.setUpdatedAt(DateUtils.nowUTC().getTime());
        sourceMapping.androidCalendar = String.valueOf(eventId);
        return sourceMapping;
    }

    public Long getUpdatedAt() {
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
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public Long getCreatedAt() {
        return createdAt;
    }

    public String getAndroidCalendar() {
        return androidCalendar;
    }

    public void setAndroidCalendar(String googleCalendar) {
        this.androidCalendar = String.valueOf(googleCalendar);
    }
}
