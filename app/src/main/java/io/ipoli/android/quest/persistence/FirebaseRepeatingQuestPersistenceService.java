package io.ipoli.android.quest.persistence;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.RepeatingQuest;
import rx.Observable;

import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebaseRepeatingQuestPersistenceService extends BaseFirebasePersistenceService<RepeatingQuest> implements RepeatingQuestPersistenceService {

    public FirebaseRepeatingQuestPersistenceService(Context context, Bus eventBus) {
        super(context, eventBus);
    }

    @Override
    protected GenericTypeIndicator<Map<String, RepeatingQuest>> getGenericMapIndicator() {
        return new GenericTypeIndicator<Map<String, RepeatingQuest>>() {

        };
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
    public void findAllForChallenge(Challenge challenge, OnDataChangedListener<List<RepeatingQuest>> listener) {
        Query query = getCollectionReference().equalTo(challenge.getId(), "challengeId");
        listenForSingleListChange(query, listener);
    }

    @Override
    public void findActiveForChallenge(Challenge challenge, OnDataChangedListener<List<RepeatingQuest>> listener) {
        Query query = getCollectionReference().equalTo(challenge.getId(), "challengeId");
        listenForQuery(query, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<RepeatingQuest> repeatingQuests = getListFromMapSnapshot(dataSnapshot);
                List<RepeatingQuest> filtered = Observable.from(repeatingQuests)
                        .filter(rq -> rq.getRecurrence().getDtend() == null || rq.getRecurrence().getDtend().getTime() >= toStartOfDayUTC(LocalDate.now()).getTime())
                        .toList().toBlocking().single();
                listener.onDataChanged(filtered);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public List<RepeatingQuest> findActiveNotForChallenge(String query, Challenge challenge) {
        return null;
    }

    @Override
    public void findByChallenge(Challenge challenge, OnDataChangedListener<List<RepeatingQuest>> listener) {
        Query query = getCollectionReference().equalTo(challenge.getId(), "challengeId");
        listenForSingleChange(query, createListListener(listener));
    }
}
