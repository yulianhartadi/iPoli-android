package io.ipoli.android.store.events;

import io.ipoli.android.store.Pet;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public class UsePetEvent {
    public final Pet pet;

    public UsePetEvent(Pet pet) {
        this.pet = pet;
    }
}
