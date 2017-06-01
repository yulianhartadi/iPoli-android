package io.ipoli.android.store.viewmodels;

import android.content.Context;
import android.support.annotation.DrawableRes;

import org.threeten.bp.LocalDate;

import io.ipoli.android.player.PetAvatar;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/26/16.
 */
public class PetViewModel {
    private String name;

    private int price;

    @DrawableRes
    private int picture;

    @DrawableRes
    private int pictureState;

    private final boolean isCurrent;

    private PetAvatar petAvatar;
    private final LocalDate boughtDate;

    public PetViewModel(Context context, PetAvatar petAvatar, @DrawableRes int pictureState) {
        this(context, petAvatar, pictureState, null, false);
    }

    public PetViewModel(Context context, PetAvatar petAvatar, @DrawableRes int pictureState, LocalDate boughtDate, boolean isCurrent) {
        this.petAvatar = petAvatar;
        this.boughtDate = boughtDate;
        this.isCurrent = isCurrent;
        this.name = context.getString(petAvatar.name);
        this.price = petAvatar.price;
        this.picture = petAvatar.picture;
        this.pictureState = pictureState;
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

    public int getPictureState() {
        return pictureState;
    }

    public PetAvatar getPetAvatar() {
        return petAvatar;
    }

    public boolean isBought() {
        return boughtDate != null;
    }

    public LocalDate getBoughtDate() {
        return boughtDate;
    }

    public boolean isCurrent() {
        return isCurrent;
    }
}
