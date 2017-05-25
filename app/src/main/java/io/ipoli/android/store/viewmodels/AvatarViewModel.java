package io.ipoli.android.store.viewmodels;

import android.support.annotation.DrawableRes;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public class AvatarViewModel {
    private String name;

    private int price;

    @DrawableRes
    private int picture;

    private String pictureName;

    public AvatarViewModel(String name, int price, @DrawableRes int picture, String pictureName) {
        this.name = name;
        this.price = price;
        this.picture = picture;
        this.pictureName = pictureName;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getPicture() {
        return picture;
    }

    public String getPictureName() {
        return pictureName;
    }
}
