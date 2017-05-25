package io.ipoli.android.store.events;

import io.ipoli.android.store.Pet;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public class SelectPetEvent {
    public final Pet pet;

    public SelectPetEvent(Pet pet) {
        this.pet = pet;
    }
}
