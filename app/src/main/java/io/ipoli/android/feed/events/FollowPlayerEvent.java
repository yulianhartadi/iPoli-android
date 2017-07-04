package io.ipoli.android.feed.events;

import io.ipoli.android.feed.data.Profile;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/3/17.
 */
public class FollowPlayerEvent {
    public final Profile profile;

    public FollowPlayerEvent(Profile profile) {
        this.profile = profile;
    }
}
