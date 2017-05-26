package io.ipoli.android.store.viewmodels;

import android.content.Context;
import android.support.annotation.DrawableRes;

import org.threeten.bp.LocalDate;

import io.ipoli.android.store.Avatar;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public class AvatarViewModel {
    private final Avatar avatar;

    private final LocalDate boughtDate;

    private final String name;

    private final int price;

    @DrawableRes
    private final int picture;

    public AvatarViewModel(Context context, Avatar avatar) {
        this(context, avatar, null);
    }

    public AvatarViewModel(Context context, Avatar avatar, LocalDate boughtDate) {
        this.avatar = avatar;
        this.boughtDate = boughtDate;
        name = context.getString(avatar.name);
        price = avatar.price;
        picture = avatar.picture;
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

    public Avatar getAvatar() {
        return avatar;
    }

    public LocalDate getBoughtDate() {
        return boughtDate;
    }

    public boolean isBought() {
        return boughtDate != null;
    }
}
