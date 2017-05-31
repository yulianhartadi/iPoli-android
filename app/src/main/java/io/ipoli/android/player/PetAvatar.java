package io.ipoli.android.player;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public enum PetAvatar {
    SEAL(1, 2, R.string.pet_seal, R.drawable.pet_1, R.drawable.pet_1_head),
    DONKEY(2, 1, R.string.pet_donkey, R.drawable.pet_2, R.drawable.pet_2_head),
    ELEPHANT(3, 1, R.string.pet_elephant, R.drawable.pet_3, R.drawable.pet_3_head),
    BEAVER(4, 1, R.string.pet_beaver, R.drawable.pet_4, R.drawable.pet_4_head),
    CHICKEN(5, 1, R.string.pet_chicken, R.drawable.pet_5, R.drawable.pet_5_head);

    public final int code;

    public final int price;

    @StringRes
    public final int name;

    @DrawableRes
    public final int picture;

    @DrawableRes
    public final int headPicture;

    PetAvatar(int code, int price, @StringRes int name, @DrawableRes int picture, @DrawableRes int headPicture) {
        this.code = code;
        this.price = price;
        this.name = name;
        this.picture = picture;
        this.headPicture = headPicture;
    }

    public static PetAvatar get(int code) {
        for (PetAvatar petAvatar : values()) {
            if (petAvatar.code == code) {
                return petAvatar;
            }
        }
        return null;
    }
}
