package io.ipoli.android.player;

import java.util.Date;
import java.util.UUID;

import io.ipoli.android.app.net.RemoteObject;
import io.ipoli.android.app.utils.DateUtils;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class Player extends RealmObject implements RemoteObject<Player> {

    @Required
    @PrimaryKey
    private String id;
    private String experience;
    private String experienceForNextLevel;
    private Integer level;
    private Long coins;
    private String avatar;
    private String timezone;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    private boolean needsSyncWithRemote;
    private boolean isRemoteObject;

    public Player() {
    }

    public Player(String experience, int level, String avatar) {
        this.id = UUID.randomUUID().toString();
        this.experience = experience;
        this.level = level;
        this.avatar = avatar;
        this.createdAt = DateUtils.nowUTC();
        this.updatedAt = DateUtils.nowUTC();
        this.needsSyncWithRemote = true;
        this.isRemoteObject = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public void markUpdated() {
        setNeedsSync();
        setUpdatedAt(DateUtils.nowUTC());
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getExperienceForNextLevel() {
        return experienceForNextLevel;
    }

    public void setExperienceForNextLevel(String experienceForNextLevel) {
        this.experienceForNextLevel = experienceForNextLevel;
    }

    public Long getCoins() {
        return coins;
    }

    public void setCoins(Long coins) {
        this.coins = coins;
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

    @Override
    public void setRemoteObject() {
        isRemoteObject = true;
    }

    @Override
    public boolean isRemoteObject() {
        return isRemoteObject;
    }
}