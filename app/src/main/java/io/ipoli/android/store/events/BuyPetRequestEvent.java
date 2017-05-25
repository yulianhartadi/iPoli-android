package io.ipoli.android.store.events;

import io.ipoli.android.store.Pet;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/27/16.
 */
public class BuyPetRequestEvent {
    public final Pet pet;

    public BuyPetRequestEvent(Pet pet) {
        this.pet = pet;
    }
}
