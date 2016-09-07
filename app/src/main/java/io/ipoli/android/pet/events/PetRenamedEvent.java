package io.ipoli.android.pet.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/31/16.
 */
public class PetRenamedEvent {
    public final String name;

    public PetRenamedEvent(String name) {
        this.name = name;
    }
}
