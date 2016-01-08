package io.ipoli.android.quest;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class Quest extends RealmObject {

    @PrimaryKey
    private String id;

    @Required
    private String name;

    @Required
    private Date createdAt;

    @Required
    private String status;

    private Date due;

    public Quest() {
    }

    public enum Status {
        UNPLANNED, PLANNED, STARTED, COMPLETED
    }

    public Quest(String name) {
        this.id = UUID.randomUUID().toString();
        this.status = Status.UNPLANNED.name();
        this.name = name;
        this.createdAt = new Date();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getDue() {
        return due;
    }

    public void setDue(Date due) {
        this.due = due;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
