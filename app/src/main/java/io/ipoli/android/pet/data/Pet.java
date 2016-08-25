package io.ipoli.android.pet.data;

import com.google.firebase.database.IgnoreExtraProperties;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/24/16.
 */
@IgnoreExtraProperties
public class Pet extends PersistedObject {

    private String name;
    private Integer healthPointsPercentage;
    private Integer experienceBonusPercentage;
    private Integer coinsBonusPercentage;
    private String picture;
    private String backgroundPicture;

    public Pet() {

    }

    public Pet(String name, String picture, String backgroundPicture, Integer healthPointsPercentage) {
        this.name = name;
        this.picture = picture;
        this.backgroundPicture = backgroundPicture;
        setHealthPointsPercentage(healthPointsPercentage);
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getBackgroundPicture() {
        return backgroundPicture;
    }

    public void setBackgroundPicture(String backgroundPicture) {
        this.backgroundPicture = backgroundPicture;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getHealthPointsPercentage() {
        return healthPointsPercentage;
    }

    public void setHealthPointsPercentage(Integer healthPointsPercentage) {
        this.healthPointsPercentage = Math.max(0, Math.min(100, healthPointsPercentage));
        updateExperienceBonusPercentage();
        updateCoinsBonusPercentage();
    }

    public Integer getExperienceBonusPercentage() {
        return experienceBonusPercentage;
    }

    public void setExperienceBonusPercentage(Integer experienceBonusPercentage) {
        this.experienceBonusPercentage = Math.max(0, Math.min(Constants.MAX_PET_XP_BONUS, experienceBonusPercentage));
    }

    public Integer getCoinsBonusPercentage() {
        return coinsBonusPercentage;
    }

    public void setCoinsBonusPercentage(Integer coinsBonusPercentage) {
        this.coinsBonusPercentage = Math.max(0, Math.min(Constants.MAX_PET_COIN_BONUS, coinsBonusPercentage));
    }

    public void addHealthPoints(int healthPoints) {
        setHealthPointsPercentage(getHealthPointsPercentage() - healthPoints);
    }

    private void updateCoinsBonusPercentage() {
        setCoinsBonusPercentage((int) Math.floor(getHealthPointsPercentage() * Constants.COINS_BONUS_PERCENTAGE_OF_HP / 100.0));
    }

    private void updateExperienceBonusPercentage() {
        setExperienceBonusPercentage((int) Math.floor(getHealthPointsPercentage() * Constants.XP_BONUS_PERCENTAGE_OF_HP / 100.0));
    }
}
