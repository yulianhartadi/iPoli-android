package io.ipoli.android.feed.ui;

import io.ipoli.android.feed.data.Profile;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/3/17.
 */
public class ProfileBinder {

    private final ProfileViewHolder holder;
    private final Profile profile;
    private final String playerId;

    private ProfileBinder(ProfileViewHolder holder, Profile profile, String playerId) {
        this.holder = holder;
        this.profile = profile;
        this.playerId = playerId;
    }

    protected void bind() {
    }

    public static void bind(ProfileViewHolder holder, Profile profile, String playerId) {
        new ProfileBinder(holder, profile, playerId).bind();
    }
}
