package io.ipoli.android.store.events;

import io.ipoli.android.store.viewmodels.PetViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/27/16.
 */
public class BuyPetRequestEvent {
    public final PetViewModel petViewModel;

    public BuyPetRequestEvent(PetViewModel petViewModel) {
        this.petViewModel = petViewModel;
    }
}
