package io.ipoli.android.app.persistence;

import java.util.Date;

import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public abstract class PersistedObject {

    protected String id;
    protected Date createdAt;
    protected Date updatedAt;
    protected boolean isDeleted;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public abstract boolean getIsDeleted();

    public abstract Date getCreatedAt();

    public abstract Date getUpdatedAt();

    public void markUpdated() {
        setUpdatedAt(DateUtils.nowUTC());
    }

    public void markDeleted() {
        isDeleted = true;
        setUpdatedAt(DateUtils.nowUTC());
    }
}
