package io.ipoli.android.player.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/4/17.
 */

public class PlayerUnfollowedEvent {
    public final String follower;
    public final String following;

    public PlayerUnfollowedEvent(String follower, String following) {
        this.follower = follower;
        this.following = following;
    }
}
