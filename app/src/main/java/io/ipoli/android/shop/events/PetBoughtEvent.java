package io.ipoli.android.shop.events;

import io.ipoli.android.shop.viewmodels.PetViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/27/16.
 */
public class PetBoughtEvent {
    public final PetViewModel petViewModel;

    public PetBoughtEvent(PetViewModel petViewModel) {
        this.petViewModel = petViewModel;
    }
}
