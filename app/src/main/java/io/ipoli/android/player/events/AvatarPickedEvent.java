package io.ipoli.android.player.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/2/16.
 */
public class AvatarPickedEvent {
    public final String avatarName;

    public AvatarPickedEvent(String avatarName) {
        this.avatarName = avatarName;
    }
}
