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
    private int picture;

    public StoreViewModel(StoreItemType type, String title, @DrawableRes int picture) {
        this.type = type;
        this.title = title;
        this.picture = picture;
    }

    public String getTitle() {
        return title;
    }

    public int getPicture() {
        return picture;
    }

    public StoreItemType getType() {
        return type;
    }
}
