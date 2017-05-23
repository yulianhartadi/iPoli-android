package io.ipoli.android.store.viewmodels;

import android.support.annotation.DrawableRes;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class UpgradeViewModel {
    private String title;
    private String shortDescription;
    private String longDescription;
    private int price;
    private int image;

    public UpgradeViewModel(String title, String shortDescription, String longDescription, int price, @DrawableRes int image) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.price = price;
        this.image = image;
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
}
