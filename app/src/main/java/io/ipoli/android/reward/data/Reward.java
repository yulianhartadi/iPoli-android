package io.ipoli.android.reward.data;

import java.util.Date;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class Reward extends PersistedObject {


    private String name;

    private String description;

    private Integer price;

    public Reward() {
    }

    public Reward(String name, Integer price) {
        this.name = name;
        this.price = price;
        createdAt = DateUtils.nowUTC();
        updatedAt = DateUtils.nowUTC();
        isDeleted = false;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }
}
