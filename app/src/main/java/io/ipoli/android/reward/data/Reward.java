package io.ipoli.android.reward.data;

import com.google.firebase.database.Exclude;

import java.util.Date;

import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class Reward {

    private String id;

    private String name;

    private String description;

    private Integer price;

    private Date createdAt;

    private Date updatedAt;

    private boolean isDeleted;

    public Reward() {
    }

    public Reward(String name, Integer price) {
        this.name = name;
        this.price = price;
        createdAt = DateUtils.nowUTC();
        updatedAt = DateUtils.nowUTC();
        isDeleted = false;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public String getId() {
        return id;
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

    public void markDeleted() {
        updatedAt = DateUtils.nowUTC();
        isDeleted = true;
    }
}
