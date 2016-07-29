package io.ipoli.android.quest.data;

import com.google.firebase.database.Exclude;

import java.util.Date;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/26/16.
 */
public class SubQuest extends PersistedObject {

    private String name;

    private Long completedAt;

    private Integer completedAtMinute;

    public SubQuest() {
    }

    public SubQuest(String name) {
        this.name = name;
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Exclude
    public Date getCompletedAtDate() {
        return completedAt != null ? new Date(completedAt) : null;
    }

    @Exclude
    public void setCompletedAtDate(Date completedAtDate) {
        completedAt = completedAtDate != null ? completedAtDate.getTime() : null;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getCompletedAtMinute() {
        return completedAtMinute;
    }

    public void setCompletedAtMinute(Integer completedAtMinute) {
        this.completedAtMinute = completedAtMinute;
    }

    public boolean isCompleted() {
        return getCompletedAtDate() != null;
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
}
