package io.ipoli.android.pet.persistence;

import android.os.Handler;
import android.os.Looper;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.View;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.BaseCouchbasePersistenceService;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/4/17.
 */

public class CouchbasePetPersistenceService extends BaseCouchbasePersistenceService<Pet> implements PetPersistenceService {

    private final LocalStorage localStorage;
    private final View allPetsView;

    public CouchbasePetPersistenceService(Database database, ObjectMapper objectMapper, LocalStorage localStorage) {
        super(database, objectMapper);
        this.localStorage = localStorage;

        allPetsView = database.getView("pets/all");
        if (allPetsView.getMap() == null) {
            allPetsView.setMap((document, emitter) -> {
                String type = (String) document.get("type");
                if (Pet.TYPE.equals(type)) {
                    emitter.emit(document.get("_id"), document);
                }
            }, "1.0");
        }
    }

    @Override
    protected Class<Pet> getModelClass() {
        return Pet.class;
    }

    @Override
    public void save(Pet pet) {
        super.save(pet);
        localStorage.saveInt(Constants.KEY_XP_BONUS_PERCENTAGE, pet.getExperienceBonusPercentage());
        localStorage.saveInt(Constants.KEY_COINS_BONUS_PERCENTAGE, pet.getCoinsBonusPercentage());
    }

    @Override
    public void find(OnDataChangedListener<Pet> listener) {
        Query query = allPetsView.createQuery();
        try {
            QueryEnumerator enumerator = query.run();
            List<Pet> result = new ArrayList<>();
            while (enumerator.hasNext()) {
                result.add(toObject(enumerator.next().getValue()));
            }
            listener.onDataChanged(result.get(0));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void listen(OnDataChangedListener<Pet> listener) {
        LiveQuery query = allPetsView.createQuery().toLiveQuery();
        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                List<Pet> result = new ArrayList<>();
                QueryEnumerator enumerator = event.getRows();
                while (enumerator.hasNext()) {
                    result.add(toObject(enumerator.next().getValue()));
                }
                new Handler(Looper.getMainLooper()).post(() -> listener.onDataChanged(result.get(0)));

            }
        };
        query.addChangeListener(changeListener);
        query.start();
        queryToListener.put(query, changeListener);
    }
}
