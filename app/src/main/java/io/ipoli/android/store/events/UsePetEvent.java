package io.ipoli.android.store.events;

import io.ipoli.android.store.PetAvatar;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public class UsePetEvent {
    public final PetAvatar petAvatar;

    public UsePetEvent(PetAvatar petAvatar) {
        this.petAvatar = petAvatar;
    }
}
