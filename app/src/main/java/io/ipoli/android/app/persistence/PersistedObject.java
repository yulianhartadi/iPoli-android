package io.ipoli.android.app.persistence;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public abstract class PersistedObject {

    @JsonProperty(value = "_id")
    protected String id;
    protected String type;
    protected Long createdAt;
    protected Long updatedAt;

    public abstract void setId(String id);

    public abstract String getId();

    public abstract void setCreatedAt(Long createdAt);

    public abstract void setUpdatedAt(Long updatedAt);

    public abstract Long getCreatedAt();

    public abstract Long getUpdatedAt();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void markUpdated() {
        setUpdatedAt(DateUtils.nowUTC().getTime());
    }
}
