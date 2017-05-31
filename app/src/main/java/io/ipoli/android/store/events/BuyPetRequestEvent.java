package io.ipoli.android.store.events;

import io.ipoli.android.player.PetAvatar;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/27/16.
 */
public class BuyPetRequestEvent {
    public final PetAvatar petAvatar;

    public BuyPetRequestEvent(PetAvatar petAvatar) {
        this.petAvatar = petAvatar;
    }
}
