package io.ipoli.android.app.persistence;

import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public abstract class PersistedObject {

    protected String id;
    protected Long createdAt;
    protected Long updatedAt;

    public abstract void setId(String id);

    public abstract String getId();

    public abstract void setCreatedAt(Long createdAt);

    public abstract void setUpdatedAt(Long updatedAt);


    public abstract Long getCreatedAt();

    public abstract Long getUpdatedAt();

    public void markUpdated() {
        setUpdatedAt(DateUtils.nowUTC().getTime());
    }
}
