package io.ipoli.android.quest.data;

import java.util.Date;
import java.util.UUID;

import io.ipoli.android.app.utils.DateUtils;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/1/16.
 */
public class QuestReward extends RealmObject {

    @Required
    @PrimaryKey
    private String id;

    private Long experience;
    private Long coins;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    public QuestReward() {

    }

    public QuestReward(long experience, long coins) {
        this.id = UUID.randomUUID().toString();
        this.experience = experience;
        this.coins = coins;
        this.createdAt = DateUtils.nowUTC();
        this.updatedAt = DateUtils.nowUTC();
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getExperience() {
        return experience;
    }

    public Long getCoins() {
        return coins;
    }
}
