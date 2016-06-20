package io.ipoli.android.reward.data;

import java.util.Date;

import io.ipoli.android.app.net.RemoteObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IDGenerator;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class Reward extends RealmObject implements RemoteObject<Reward> {
    @Required
    @PrimaryKey
    private String id;

    @Required
    private String name;

    private String description;

    @Required
    private Integer price;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    private Boolean needsSyncWithRemote;
    private String remoteId;
    private boolean isDeleted;

    public Reward() {
    }

    public Reward(String name, Integer price) {
        this.id = IDGenerator.generate();
        this.name = name;
        this.price = price;
        createdAt = DateUtils.nowUTC();
        updatedAt = DateUtils.nowUTC();
        needsSyncWithRemote = true;
        isDeleted = false;
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
    public void markUpdated() {
        setNeedsSync();
        setUpdatedAt(DateUtils.nowUTC());
    }

    @Override
    public void setNeedsSync() {
        needsSyncWithRemote = true;
    }

    @Override
    public boolean needsSyncWithRemote() {
        return needsSyncWithRemote;
    }

    @Override
    public void setSyncedWithRemote() {
        needsSyncWithRemote = false;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    @Override
    public String getRemoteId() {
        return remoteId;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public void markDeleted() {
        isDeleted = true;
        markUpdated();
    }

    @Override
    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }
}
