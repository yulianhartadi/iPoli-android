package io.ipoli.android.quest.persistence;

import android.content.Context;

import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.RepeatingQuest;

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
    public void findAllNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener) {

    }

    @Override
    public void findNonFlexibleNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener) {
        Query query = getCollectionReference().equalTo(0, "recurrence/flexibleCount");
        listenForListChange(query, listener, data ->
                data.filter(rq -> rq.getRecurrence().getDtend() == null
                        || rq.getRecurrence().getDtend().getTime() >= toStartOfDayUTC(LocalDate.now()).getTime())
        );
    }

    @Override
    public void findByExternalSourceMappingId(String source, String sourceId, OnDataChangedListener<RepeatingQuest> listener) {
        Query query = getCollectionReference().equalTo(sourceId, "sourceMapping/" + source);
        listenForSingleModelChange(query, listener);
    }

    @Override
    public void findAllForChallenge(Challenge challenge, OnDataChangedListener<List<RepeatingQuest>> listener) {
        Query query = getCollectionReference().equalTo(challenge.getId(), "challengeId");
        listenForSingleListChange(query, listener);
    }

    @Override
    public void findActiveForChallenge(Challenge challenge, OnDataChangedListener<List<RepeatingQuest>> listener) {
        Query query = getCollectionReference().equalTo(challenge.getId(), "challengeId");
        listenForListChange(query, listener, data ->
                data.filter(rq -> rq.getRecurrence().getDtend() == null
                        || rq.getRecurrence().getDtend().getTime() >= toStartOfDayUTC(LocalDate.now()).getTime())
        );
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
