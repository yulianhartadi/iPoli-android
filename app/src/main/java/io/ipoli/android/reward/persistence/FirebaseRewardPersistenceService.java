package io.ipoli.android.reward.persistence;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.quest.persistence.OnDatabaseChangedListener;
import io.ipoli.android.reward.data.Reward;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebaseRewardPersistenceService extends BaseFirebasePersistenceService<Reward> implements RewardPersistenceService {

    public FirebaseRewardPersistenceService(Context context, Bus eventBus) {
        super(context, eventBus);
    }

    @Override
    protected Class<Reward> getModelClass() {
        return Reward.class;
    }

    @Override
    protected String getCollectionName() {
        return "rewards";
    }

    @Override
    public void findAll(OnDatabaseChangedListener<List<Reward>> listener) {
        Query query = getCollectionReference().orderByChild("isDeleted").equalTo(false);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Reward> rewards = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Reward reward = snapshot.getValue(getModelClass());
                    reward.setId(snapshot.getKey());
                    rewards.add(reward);
                }
                listener.onDatabaseChanged(rewards);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        valueListeners.put(query.getRef(), valueEventListener);
        query.addValueEventListener(valueEventListener);
    }

}
