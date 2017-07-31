package io.ipoli.android.store.events;

import io.ipoli.android.player.data.Avatar;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/25/17.
 */
public class AvatarChangedEvent {

    public final Avatar avatar;

    public AvatarChangedEvent(Avatar avatar) {
        this.avatar = avatar;
    }
}
