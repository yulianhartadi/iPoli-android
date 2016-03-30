package io.ipoli.android.player;

import java.util.UUID;

import io.ipoli.android.app.net.RemoteObject;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

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

    private String remoteId;
    private boolean needsSyncWithRemote;

    public Player() {
    }

    public Player(int experience, int level, String avatar) {
        this.id = UUID.randomUUID().toString();
        this.experience = experience;
        this.level = level;
        this.avatar = avatar;
        this.needsSyncWithRemote = true;
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
    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    @Override
    public String getRemoteId() {
        return remoteId;
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
        this.needsSyncWithRemote = false;
    }

    @Override
    public void markUpdated() {

    }
}