package io.ipoli.android.challenge.persistence;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Bus;

import java.util.ArrayList;
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
        return new GenericTypeIndicator<Map<String, Challenge>>() {

        };
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
    public void findAllNotCompleted(OnDataChangedListener<List<Challenge>> listener) {
        Query query = getCollectionReference().orderByChild("endDate/time");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Challenge> challenges = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.hasChild("completedAt")) {
                        continue;
                    }
                    challenges.add(snapshot.getValue(getModelClass()));
                }
                listener.onDataChanged(challenges);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        valueListeners.put(query.getRef(), valueEventListener);
        query.addValueEventListener(valueEventListener);
    }

    @Override
    public List<Challenge> findAllNotCompleted() {
        return null;
    }

}
