package io.ipoli.android.quest.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/26/16.
 */
public class SubQuest {

    private String name;

    private Long completedAt;

    private Integer completedAtMinute;

    public SubQuest() {
    }

    public SubQuest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public Date getCompletedAtDate() {
        return completedAt != null ? new Date(completedAt) : null;
    }

    @JsonIgnore
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

    @JsonIgnore
    public boolean isCompleted() {
        return getCompletedAtDate() != null;
    }
}
