package io.ipoli.android.shop.viewmodels;

import android.support.annotation.DrawableRes;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/26/16.
 */
public class PetViewModel {
    private String description;

    private int price;

    @DrawableRes
    private int picture;

    @DrawableRes
    private int pictureState;


    public PetViewModel(String description, int price, @DrawableRes int picture, @DrawableRes int pictureState) {
        this.description = description;
        this.price = price;
        this.picture = picture;
        this.pictureState = pictureState;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPicture() {
        return picture;
    }

    public void setPicture(int picture) {
        this.picture = picture;
    }

    public int getPictureState() {
        return pictureState;
    }

    public void setPictureState(int pictureState) {
        this.pictureState = pictureState;
    }
}
