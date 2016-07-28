package io.ipoli.android.player;

import com.google.firebase.database.Exclude;

import java.math.BigInteger;
import java.util.Date;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class Player extends PersistedObject {

    @Exclude
    private String id;
    private String experience;
    private Integer level;
    private Long coins;
    private String avatar;
    private String timezone;

    private Date createdAt;

    private Date updatedAt;

    private boolean isDeleted;

    public Player() {
    }

    public Player(String experience, int level, String avatar) {
        this.experience = experience;
        this.level = level;
        this.avatar = avatar;
        this.createdAt = DateUtils.nowUTC();
        this.updatedAt = DateUtils.nowUTC();
        this.isDeleted = false;
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

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public void setIsDeleted(boolean deleted) {
        this.isDeleted = deleted;
    }

    @Override
    public boolean getIsDeleted() {
        return isDeleted;
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

    public Long getCoins() {
        return coins;
    }

    public void setCoins(Long coins) {
        this.coins = coins;
    }

    public void addExperience(long experience) {
        this.experience = new BigInteger(this.experience).add(new BigInteger(String.valueOf(experience))).toString();
    }

    public void removeExperience(long experience) {
        BigInteger newXP = new BigInteger(this.experience).subtract(new BigInteger(String.valueOf(experience)));
        if (newXP.compareTo(BigInteger.ZERO) < 0) {
            newXP = BigInteger.ZERO;
        }
        this.experience = newXP.toString();
    }

    public void addCoins(long coins) {
        this.coins += coins;
    }

    public void removeCoins(long coins) {
        this.coins = Math.max(0, this.coins - coins);
    }
}