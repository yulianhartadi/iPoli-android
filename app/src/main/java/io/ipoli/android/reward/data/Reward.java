package io.ipoli.android.reward.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class Reward extends PersistedObject {

    public static final String TYPE = "reward";

    @JsonProperty(value = "_id")
    private String id;
    private String name;

    private String description;

    private Integer price;

    private Long createdAt;
    private Long updatedAt;

    private String type;

    public Reward() {
    }

    public Reward(String name, Integer price) {
        this.name = name;
        this.price = price;
        this.type = TYPE;
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
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

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}