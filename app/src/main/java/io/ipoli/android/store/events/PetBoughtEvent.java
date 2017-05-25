package io.ipoli.android.store.events;

import io.ipoli.android.store.Pet;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/27/16.
 */
public class PetBoughtEvent {
    public final Pet pet;

    public PetBoughtEvent(Pet pet) {
        this.pet = pet;
    }
}
