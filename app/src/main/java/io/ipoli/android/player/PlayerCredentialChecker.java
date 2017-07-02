package io.ipoli.android.player;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/2/17.
 */
public class PlayerCredentialChecker {

    public enum Status {
        AUTHORIZED, GUEST, NO_USERNAME
    }

    public static Status checkStatus(Player player) {
        if (player.isGuest()) {
            return Status.GUEST;
        }
        if (player.doesNotHaveUsername()) {
            return Status.NO_USERNAME;
        }
        return Status.AUTHORIZED;
    }
}
