package io.ipoli.android.store.events;

import io.ipoli.android.player.Avatar;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/26/17.
 */

public class UseAvatarEvent {
    public final Avatar avatar;

    public UseAvatarEvent(Avatar avatar) {
        this.avatar = avatar;
    }
}
