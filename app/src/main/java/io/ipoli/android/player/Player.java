package io.ipoli.android.player;

import com.google.firebase.database.IgnoreExtraProperties;

import java.math.BigInteger;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
@IgnoreExtraProperties
public class Player extends PersistedObject {

    private String uid;
    private String experience;
    private Integer level;
    private Long coins;
    private String avatar;
    private String timezone;

    public Player() {
    }

    public Player(String uid, String experience, int level, long coins, String avatar) {
        this.uid = uid;
        this.experience = experience;
        this.level = level;
        this.coins = coins;
        this.avatar = avatar;
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
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

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedAt() {
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}