package io.ipoli.android.quest.data;

import java.util.Date;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IDGenerator;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class SourceMapping extends RealmObject{

    @Required
    @PrimaryKey
    private String id;

    private String androidCalendar;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    public SourceMapping() {
    }

    public static SourceMapping fromGoogleCalendar(long eventId) {
        SourceMapping sourceMapping = new SourceMapping();
        sourceMapping.id = IDGenerator.generate();
        sourceMapping.createdAt = DateUtils.nowUTC();
        sourceMapping.updatedAt = DateUtils.nowUTC();
        sourceMapping.androidCalendar = String.valueOf(eventId);
        return sourceMapping;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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
