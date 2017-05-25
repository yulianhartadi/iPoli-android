package io.ipoli.android.store.viewmodels;

import android.content.Context;
import android.support.annotation.DrawableRes;

import io.ipoli.android.store.Pet;

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

    private Pet pet;

    public PetViewModel(Context context, Pet pet, @DrawableRes int pictureState) {
        this.pet = pet;
        this.name = context.getString(pet.name);
        this.price = pet.price;
        this.picture = pet.picture;
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

    public Pet getPet() {
        return pet;
    }
}
