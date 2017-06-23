package io.ipoli.android.reward.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class Reward extends PersistedObject {

    public static final String TYPE = "reward";

    private String name;

    private String description;

    private Integer price;

    private List<RewardPurchase> purchases;

    public Reward() {
        super(TYPE);
    }

    public Reward(String name, Integer price) {
        super(TYPE);
        this.name = name;
        this.price = price;
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
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

    public List<RewardPurchase> getPurchases() {
        if (purchases == null) {
            purchases = new ArrayList<>();
        }
        return purchases;
    }

    public void setPurchases(List<RewardPurchase> purchases) {
        this.purchases = purchases;
    }

    @JsonIgnore
    public int getPurchaseCount() {
        return getPurchases().size();
    }

    @JsonIgnore
    public LocalDate getLastPurchaseDate() {
        if (getPurchases().isEmpty()) {
            return null;
        }
        return DateUtils.fromMillis(getPurchases().get(getPurchaseCount() - 1).getDate());
    }

    @JsonIgnore
    public void addPurchase(LocalDate date, int minute) {
        getPurchases().add(new RewardPurchase(DateUtils.toMillis(date), minute));
    }
}