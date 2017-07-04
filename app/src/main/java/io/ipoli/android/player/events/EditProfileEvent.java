package io.ipoli.android.player.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/4/17.
 */

public class EditProfileEvent {
    public final String playerId;

    public EditProfileEvent(String playerId) {
        this.playerId = playerId;
    }
}
