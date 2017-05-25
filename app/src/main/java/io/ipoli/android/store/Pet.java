package io.ipoli.android.store;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public enum Pet {
    SEAL(1, 2, R.string.pet_seal, R.drawable.pet_1),
    DONKEY(2, 1, R.string.pet_donkey, R.drawable.pet_2),
    ELEPHANT(3, 1, R.string.pet_elephant, R.drawable.pet_3),
    BEAVER(4, 1, R.string.pet_beaver, R.drawable.pet_4),
    CHICKEN(5, 1, R.string.pet_chicken, R.drawable.pet_5);

    public final int code;

    public final int price;

    @StringRes
    public final int name;

    @DrawableRes
    public final int picture;

    Pet(int code, int price, @StringRes int name, @DrawableRes int picture) {
        this.code = code;
        this.price = price;
        this.name = name;
        this.picture = picture;
    }

}
