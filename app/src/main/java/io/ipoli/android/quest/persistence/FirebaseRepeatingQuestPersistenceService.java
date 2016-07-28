package io.ipoli.android.quest.persistence;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.reminders.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebaseRepeatingQuestPersistenceService extends BaseFirebasePersistenceService<RepeatingQuest> implements RepeatingQuestPersistenceService {

    public FirebaseRepeatingQuestPersistenceService(Context context, Bus eventBus) {
        super(context, eventBus);
    }

    @Override
    protected Class<RepeatingQuest> getModelClass() {
        return RepeatingQuest.class;
    }

    @Override
    protected String getCollectionName() {
        return "repeating-quests";
    }


    @Override
    public List<RepeatingQuest> findAllNonAllDayActiveRepeatingQuests() {
        return null;
    }

    @Override
    public void findAllNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener) {

    }

    @Override
    public void findNonFlexibleNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener) {

    }

    @Override
    public RepeatingQuest findByExternalSourceMappingId(String source, String sourceId) {
        return null;
    }

    @Override
    public List<RepeatingQuest> findAllForChallenge(Challenge challenge) {
        return null;
    }

    @Override
    public void saveReminders(RepeatingQuest repeatingQuest, List<Reminder> reminders) {

    }

    @Override
    public void findActiveForChallenge(Challenge challenge, OnDataChangedListener<List<RepeatingQuest>> listener) {

    }

    @Override
    public List<RepeatingQuest> findActiveNotForChallenge(String query, Challenge challenge) {
        return null;
    }

    @Override
    public void findNotDeleted(Challenge challenge, OnDataChangedListener<List<RepeatingQuest>> listener) {
        Query query = getCollectionReference().equalTo(challenge.getId(), "challengeId");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<RepeatingQuest> repeatingQuests = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RepeatingQuest rq = snapshot.getValue(getModelClass());
                    rq.setId(snapshot.getKey());
                    repeatingQuests.add(rq);
                }
                listener.onDataChanged(repeatingQuests);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        query.addListenerForSingleValueEvent(valueEventListener);
    }
}
