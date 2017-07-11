package io.ipoli.android.player;

import io.ipoli.android.player.data.Player;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/2/17.
 */
public class PlayerCredentialChecker {

    public static CredentialStatus checkStatus(Player player) {
        if (player.isGuest()) {
            return CredentialStatus.GUEST;
        }
        if (player.doesNotHaveUsername()) {
            return CredentialStatus.NO_USERNAME;
        }
        return CredentialStatus.AUTHORIZED;
    }
}
