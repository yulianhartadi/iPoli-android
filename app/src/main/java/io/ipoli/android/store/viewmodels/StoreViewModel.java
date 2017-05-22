package io.ipoli.android.store.viewmodels;

import android.support.annotation.DrawableRes;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

public class StoreViewModel {
    private String title;

    @DrawableRes
    private int image;

    public StoreViewModel(String title, @DrawableRes int image) {
        this.title = title;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public int getImage() {
        return image;
    }
}
