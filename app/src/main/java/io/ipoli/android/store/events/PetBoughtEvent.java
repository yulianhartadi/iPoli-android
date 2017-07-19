package io.ipoli.android.store.events;

import io.ipoli.android.player.data.PetAvatar;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/27/16.
 */
public class PetBoughtEvent {
    public final PetAvatar petAvatar;

    public PetBoughtEvent(PetAvatar petAvatar) {
        this.petAvatar = petAvatar;
    }
}
