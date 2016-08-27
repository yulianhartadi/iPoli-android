package io.ipoli.android.player;

import com.google.firebase.database.Exclude;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.avatar.Avatar;
import io.ipoli.android.pet.data.Pet;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class Player extends PersistedObject {

    private String uid;
    @Exclude
    private Pet pet;
    @Exclude
    private Avatar avatar;

    public Player() {
    }

    public Player(String uid, Pet pet, Avatar avatar) {
        this.uid = uid;
        this.pet = pet;
        this.avatar = avatar;
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
    }

    @Exclude
    public Pet getPet() {
        return pet;
    }

    @Exclude
    public void setPet(Pet pet) {
        this.pet = pet;
    }

    @Exclude
    public Avatar getAvatar() {
        return avatar;
    }

    @Exclude
    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }
}