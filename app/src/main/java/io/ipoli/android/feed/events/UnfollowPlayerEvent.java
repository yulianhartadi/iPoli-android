package io.ipoli.android.feed.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/3/17.
 */
public class UnfollowPlayerEvent {
    public final String profileId;

    public UnfollowPlayerEvent(String profileId) {
        this.profileId = profileId;
    }
}
