package io.ipoli.android.player.events;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.player.PlayerAuthenticationStatus;
import okhttp3.Cookie;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/30/17.
 */

public class PlayerSignedInEvent {
    public final String provider;
    public final PlayerAuthenticationStatus playerAuthenticationStatus;
    public final String playerId;
    public final List<Cookie> cookies;

    public PlayerSignedInEvent(String provider, PlayerAuthenticationStatus playerAuthenticationStatus, String playerId) {
        this(provider, playerAuthenticationStatus, playerId, new ArrayList<>());
    }

    public PlayerSignedInEvent(String provider, PlayerAuthenticationStatus playerAuthenticationStatus, String playerId, List<Cookie> cookies) {
        this.provider = provider;
        this.playerAuthenticationStatus = playerAuthenticationStatus;
        this.playerId = playerId;
        this.cookies = cookies;
    }
}
