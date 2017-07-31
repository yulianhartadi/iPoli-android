package io.ipoli.android.store.events;

import io.ipoli.android.player.data.PetAvatar;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/28/17.
 */
public class PetChangedEvent {
    public final PetAvatar petAvatar;

    public PetChangedEvent(PetAvatar petAvatar) {
        this.petAvatar = petAvatar;
    }
}
