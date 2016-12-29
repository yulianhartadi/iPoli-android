package io.ipoli.android.pet.persistence;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Bus;

import java.util.List;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebasePetPersistenceService extends BaseFirebasePersistenceService<Pet> implements PetPersistenceService {

    private final LocalStorage localStorage;

    public FirebasePetPersistenceService(Bus eventBus, LocalStorage localStorage) {
        super(eventBus);
        this.localStorage = localStorage;
    }

    @Override
    public void find(OnDataChangedListener<Pet> listener) {
        listenForSingleChange(getCollectionReference(), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Pet> data = getListFromMapSnapshot(dataSnapshot);
                if (data == null || data.isEmpty()) {
                    listener.onDataChanged(null);
                    return;
                }
                listener.onDataChanged(data.get(0));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void listen(OnDataChangedListener<Pet> listener) {
        listenForQuery(getCollectionReference(), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Pet> data = getListFromMapSnapshot(dataSnapshot);
                if (data == null || data.isEmpty()) {
                    listener.onDataChanged(null);
                    return;
                }
                listener.onDataChanged(data.get(0));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }, listener);
    }

    @Override
    public void save(Pet pet) {
        super.save(pet);
        localStorage.saveInt(Constants.KEY_XP_BONUS_PERCENTAGE, pet.getExperienceBonusPercentage());
        localStorage.saveInt(Constants.KEY_COINS_BONUS_PERCENTAGE, pet.getCoinsBonusPercentage());

    }

    @Override
    protected Class<Pet> getModelClass() {
        return Pet.class;
    }

    @Override
    protected String getCollectionName() {
        return "pets";
    }

    @Override
    protected DatabaseReference getCollectionReference() {
        return getPlayerReference().child(getCollectionName());
    }

    @Override
    protected GenericTypeIndicator<Map<String, Pet>> getGenericMapIndicator() {
        return new GenericTypeIndicator<Map<String, Pet>>() {

        };
    }
}
