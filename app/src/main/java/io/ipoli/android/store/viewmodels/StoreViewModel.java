package io.ipoli.android.store.viewmodels;

import android.support.annotation.DrawableRes;

import io.ipoli.android.store.StoreItemType;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

public class StoreViewModel {
    private StoreItemType type;
    private String title;

    @DrawableRes
    private int image;

    public StoreViewModel(StoreItemType type, String title, @DrawableRes int image) {
        this.type = type;
        this.title = title;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public int getImage() {
        return image;
    }

    public StoreItemType getType() {
        return type;
    }
}
