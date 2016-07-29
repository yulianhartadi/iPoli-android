package io.ipoli.android.challenge.persistence;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.squareup.otto.Bus;

import java.util.List;
import java.util.Map;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebaseChallengePersistenceService extends BaseFirebasePersistenceService<Challenge> implements ChallengePersistenceService {

    public FirebaseChallengePersistenceService(Context context, Bus eventBus) {
        super(context, eventBus);
    }

    @Override
    protected GenericTypeIndicator<Map<String, Challenge>> getGenericMapIndicator() {
        return new GenericTypeIndicator<Map<String, Challenge>>() {};
    }

    @Override
    protected Class<Challenge> getModelClass() {
        return Challenge.class;
    }

    @Override
    protected String getCollectionName() {
        return "challenges";
    }

    @Override
    protected DatabaseReference getCollectionReference() {
        return getPlayerReference().child(getCollectionName());
    }

    @Override
    public void findAllNotCompleted(OnDataChangedListener<List<Challenge>> listener) {
        Query query = getCollectionReference().orderByChild("endDate/time");
        listenForListChange(query, listener, data -> data.filter(c -> c.getCompletedAt() == null));
    }
}
