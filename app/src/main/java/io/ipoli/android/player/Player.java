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

    @PrimaryKey
    private String id;
    private int experience;
    private int level;

    private String avatar;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    public Player() {
    }

    public Player(int experience, int level, String avatar) {
        this.id = UUID.randomUUID().toString();
        this.experience = experience;
        this.level = level;
        this.avatar = avatar;
        this.createdAt = DateUtils.nowUTC();
        this.updatedAt = DateUtils.nowUTC();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
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
}