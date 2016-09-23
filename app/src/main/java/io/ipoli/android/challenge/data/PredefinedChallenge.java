package io.ipoli.android.challenge.data;

import android.support.annotation.DrawableRes;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/23/16.
 */

public class PredefinedChallenge {
    public final Challenge challenge;

    public final String description;

    @DrawableRes
    public final int picture;

    @DrawableRes
    public final int backgroundPicture;

    public PredefinedChallenge(Challenge challenge, String description, int picture, int backgroundPicture) {
        this.challenge = challenge;
        this.description = description;
        this.picture = picture;
        this.backgroundPicture = backgroundPicture;
    }
}
