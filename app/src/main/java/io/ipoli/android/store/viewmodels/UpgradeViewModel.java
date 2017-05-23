package io.ipoli.android.store.viewmodels;

import android.support.annotation.DrawableRes;

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

    public UpgradeViewModel(String title, String shortDescription, String longDescription, int price, @DrawableRes int image, Upgrade upgrade) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.price = price;
        this.image = image;
        this.upgrade = upgrade;
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
}
