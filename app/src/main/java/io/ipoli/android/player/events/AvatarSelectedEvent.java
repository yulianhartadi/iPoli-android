package io.ipoli.android.player.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/1/16.
 */
public class AvatarSelectedEvent {
    public final String avatarName;

    public AvatarSelectedEvent(String avatarName) {
        this.avatarName = avatarName;
    }
}
