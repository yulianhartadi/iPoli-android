package io.ipoli.android.pet.events;

import io.ipoli.android.player.data.PetAvatar;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/27/16.
 */
public class RevivePetRequest {
    public final PetAvatar petAvatar;

    public RevivePetRequest(PetAvatar petAvatar) {
        this.petAvatar = petAvatar;
    }
}
