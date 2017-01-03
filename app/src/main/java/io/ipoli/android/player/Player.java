package io.ipoli.android.player;

import com.google.firebase.database.Exclude;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.avatar.Avatar;
import io.ipoli.android.pet.data.Pet;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class Player extends PersistedObject {

    @Exclude
    private Pet pet;

    @Exclude
    private Avatar avatar;

    private Integer schemaVersion;

    public Player() {
    }

    public Player(Pet pet, Avatar avatar) {
        this.pet = pet;
        this.avatar = avatar;
        this.schemaVersion = Constants.SCHEMA_VERSION;
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
    }

    @Exclude
    public Pet getPet() {
        return pet;
    }

    @Exclude
    public Avatar getAvatar() {
        return avatar;
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

    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
    }
}