package io.ipoli.android.feed.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/3/17.
 */
public class FollowPlayerEvent {
    public final String profileId;

    public FollowPlayerEvent(String profileId) {
        this.profileId = profileId;
    }
}
