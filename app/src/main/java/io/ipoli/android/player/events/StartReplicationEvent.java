package io.ipoli.android.player.events;

import java.util.List;

import okhttp3.Cookie;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/17/17.
 */

public class StartReplicationEvent {
    public final List<Cookie> cookies;

    public StartReplicationEvent(List<Cookie> cookies) {
        this.cookies = cookies;
    }
}
