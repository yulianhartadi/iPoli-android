package io.ipoli.android.quest.data;

import java.util.Date;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/26/16.
 */
public class SubQuest extends PersistedObject {

    private String name;

    private Date completedAt;

    private Integer completedAtMinute;

    public SubQuest() {
    }

    public SubQuest(String name) {
        this.name = name;
        this.isDeleted = false;
        createdAt = DateUtils.nowUTC();
        updatedAt = DateUtils.nowUTC();
    }

    @Override
    public boolean getIsDeleted() {
        return isDeleted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getCompletedAtMinute() {
        return completedAtMinute;
    }

    public void setCompletedAtMinute(Integer completedAtMinute) {
        this.completedAtMinute = completedAtMinute;
    }

    public boolean isCompleted() {
        return getCompletedAt() != null;
    }

    @Override
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setIsDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
