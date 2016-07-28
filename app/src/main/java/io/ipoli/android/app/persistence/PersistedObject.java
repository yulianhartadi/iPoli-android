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

    public abstract void setCreatedAt(Date createdAt);

    public abstract void setUpdatedAt(Date updatedAt);

    public abstract void setIsDeleted(boolean deleted);

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
