package io.ipoli.android.store.viewmodels;

import android.content.Context;

import org.threeten.bp.LocalDate;

import io.ipoli.android.store.PowerUp;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class PowerUpViewModel {
    private final String title;
    private final String shortDescription;
    private final String longDescription;
    private final int price;
    private final int image;
    private final PowerUp powerUp;
    private final LocalDate expirationDate;

    public PowerUpViewModel(Context context, PowerUp powerUp) {
        this(context, powerUp, null);
    }

    public PowerUpViewModel(Context context, PowerUp powerUp, LocalDate expirationDate) {
        this.title = context.getString(powerUp.title);
        this.shortDescription = context.getString(powerUp.subTitle);
        this.longDescription = context.getString(powerUp.longDesc);
        this.price = powerUp.price;
        this.image = powerUp.picture;
        this.powerUp = powerUp;
        this.expirationDate = expirationDate;
    }

    public String getTitle() {
        return title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public int getPrice() {
        return price;
    }

    public int getImage() {
        return image;
    }

    public PowerUp getPowerUp() {
        return powerUp;
    }

    public boolean isUnlocked() {
        return expirationDate != null;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public boolean requiresUpgrade() {
        return powerUp.requiredPowerUp != null;
    }

    public PowerUp getRequiredUpgrade() {
        return powerUp.requiredPowerUp;
    }
}
