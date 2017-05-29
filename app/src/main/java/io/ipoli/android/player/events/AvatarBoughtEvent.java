package io.ipoli.android.player.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/2/16.
 */
public class AvatarBoughtEvent {
    public final String avatarName;

    public AvatarBoughtEvent(String avatarName) {
        this.avatarName = avatarName;
    }
}
