package io.ipoli.android.store.viewmodels;

import android.content.Context;

import org.threeten.bp.LocalDate;

import io.ipoli.android.player.Upgrade;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class UpgradeViewModel {
    private final String title;
    private final String shortDescription;
    private final String longDescription;
    private final int price;
    private final int image;
    private final Upgrade upgrade;
    private final boolean isBought;
    private final LocalDate boughtOn;

    public UpgradeViewModel(Context context, Upgrade upgrade, boolean isBought, LocalDate boughtOn) {
        this.title = context.getString(upgrade.getTitle());
        this.shortDescription = context.getString(upgrade.getShortDesc());
        this.longDescription = context.getString(upgrade.getLongDesc());
        this.price = upgrade.getPrice();
        this.image = upgrade.getImage();
        this.upgrade = upgrade;
        this.isBought = isBought;
        this.boughtOn = boughtOn;
    }

    public String getTitle() {
        return title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public int getPrice() {
        return price;
    }

    public int getImage() {
        return image;
    }

    public Upgrade getUpgrade() {
        return upgrade;
    }

    public boolean isBought() {
        return isBought;
    }

    public LocalDate getBoughtOn() {
        return boughtOn;
    }
}
