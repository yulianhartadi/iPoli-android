package io.ipoli.android.store.events;

import io.ipoli.android.store.Avatar;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/26/17.
 */

public class BuyAvatarRequestEvent {
    public final Avatar avatar;

    public BuyAvatarRequestEvent(Avatar avatar) {
        this.avatar = avatar;
    }
}
