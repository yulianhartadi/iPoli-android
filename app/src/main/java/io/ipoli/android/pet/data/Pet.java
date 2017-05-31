package io.ipoli.android.pet.data;

import android.support.annotation.ColorRes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.store.PetAvatar;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/24/16.
 */
public class Pet {

    private String name;
    private Integer healthPointsPercentage;
    private Integer experienceBonusPercentage;
    private Integer coinsBonusPercentage;
    private Integer rewardPointsBonusPercentage;
    private Integer avatarCode;
    private String backgroundPicture;

    public Pet() {

    }

    public Pet(String name, Integer avatarCode, String backgroundPicture, Integer healthPointsPercentage) {
        this.name = name;
        this.avatarCode = avatarCode;
        this.backgroundPicture = backgroundPicture;
        setHealthPointsPercentage(healthPointsPercentage);
    }

    public Integer getAvatarCode() {
        return avatarCode;
    }

    public void setAvatarCode(Integer avatarCode) {
        this.avatarCode = avatarCode;
    }

    @JsonIgnore
    public void setPetAvatar(PetAvatar petAvatar) {
        avatarCode = petAvatar.code;
    }

    public String getBackgroundPicture() {
        return backgroundPicture;
    }

    public void setBackgroundPicture(String backgroundPicture) {
        this.backgroundPicture = backgroundPicture;
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
        updateRewardPointsBonusPercentage();
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

    public Integer getRewardPointsBonusPercentage() {
        return rewardPointsBonusPercentage;
    }

    public void setRewardPointsBonusPercentage(Integer rewardPointsBonusPercentage) {
        this.rewardPointsBonusPercentage = Math.max(0, Math.min(Constants.MAX_PET_REWARD_POINTS_BONUS, rewardPointsBonusPercentage));
    }

    @JsonIgnore
    public void addHealthPoints(int healthPoints) {
        setHealthPointsPercentage(getHealthPointsPercentage() + healthPoints);
    }

    @JsonIgnore
    private void updateCoinsBonusPercentage() {
        setCoinsBonusPercentage((int) Math.floor(getHealthPointsPercentage() * Constants.COINS_BONUS_PERCENTAGE_OF_HP / 100.0));
    }

    @JsonIgnore
    private void updateRewardPointsBonusPercentage() {
        setRewardPointsBonusPercentage((int) Math.floor(getHealthPointsPercentage() * Constants.REWARD_POINTS_BONUS_PERCENTAGE_OF_HP / 100.0));
    }

    @JsonIgnore
    private void updateExperienceBonusPercentage() {
        setExperienceBonusPercentage((int) Math.floor(getHealthPointsPercentage() * Constants.XP_BONUS_PERCENTAGE_OF_HP / 100.0));
    }

    @JsonIgnore
    public PetAvatar getCurrentAvatar() {
        return PetAvatar.get(avatarCode);
    }

    @JsonIgnore
    public String getStateText() {
        return getState().name().toLowerCase();
    }


    @ColorRes
    @JsonIgnore
    public int getStateColor() {
        return getState().color;
    }


    @JsonIgnore
    public PetState getState() {
        if (healthPointsPercentage >= 90) {
            return PetState.AWESOME;
        }
        if (healthPointsPercentage >= 60) {
            return PetState.HAPPY;
        }
        if (healthPointsPercentage >= 35) {
            return PetState.GOOD;
        }
        if (healthPointsPercentage > 0) {
            return PetState.SAD;
        }
        return PetState.DEAD;
    }

    public enum PetState {
        AWESOME(R.color.md_green_500),
        HAPPY(R.color.md_orange_500),
        GOOD(R.color.md_yellow_500),
        SAD(R.color.md_red_500),
        DEAD(R.color.md_black);

        public final int color;

        PetState(@ColorRes int color) {
            this.color = color;
        }

        public static int getNameRes(PetState petState) {
            switch (petState) {
                case AWESOME:
                    return R.string.pet_state_awesome;
                case HAPPY:
                    return R.string.pet_state_happy;
                case GOOD:
                    return R.string.pet_state_good;
                case SAD:
                    return R.string.pet_state_sad;
                default:
                    return R.string.pet_state_dead;
            }
        }
    }
}
