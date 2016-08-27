package io.ipoli.android.pet.persistence;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public interface PetPersistenceService extends PersistenceService<Pet> {
    void find(OnDataChangedListener<Pet> listener);

    void listen(OnDataChangedListener<Pet> listener);
}