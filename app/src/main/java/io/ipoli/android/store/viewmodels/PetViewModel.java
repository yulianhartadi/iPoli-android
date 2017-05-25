package io.ipoli.android.store.viewmodels;

import android.content.Context;
import android.support.annotation.DrawableRes;

import org.threeten.bp.LocalDate;

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
    private final LocalDate boughtDate;

    public PetViewModel(Context context, Pet pet, @DrawableRes int pictureState) {
        this(context, pet, pictureState, null);
    }

    public PetViewModel(Context context, Pet pet, @DrawableRes int pictureState, LocalDate boughtDate) {
        this.pet = pet;
        this.boughtDate = boughtDate;
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

    public boolean isBought() {
        return boughtDate != null;
    }

    public LocalDate getBoughtDate() {
        return boughtDate;
    }
}
