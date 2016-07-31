package io.ipoli.android.quest.persistence;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.gson.Gson;
import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.RepeatingQuest;
import rx.Observable;
import rx.functions.Func1;

import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebaseRepeatingQuestPersistenceService extends BaseFirebasePersistenceService<RepeatingQuest> implements RepeatingQuestPersistenceService {

    public FirebaseRepeatingQuestPersistenceService(LocalStorage localStorage, Bus eventBus, Gson gson) {
        super(localStorage, eventBus, gson);
    }

    @Override
    protected DatabaseReference getCollectionReference() {
        return getPlayerReference().child(getCollectionName());
    }

    @Override
    protected GenericTypeIndicator<Map<String, RepeatingQuest>> getGenericMapIndicator() {
        return new GenericTypeIndicator<Map<String, RepeatingQuest>>() {
        };
    }

    @Override
    protected GenericTypeIndicator<List<RepeatingQuest>> getGenericListIndicator() {
        return new GenericTypeIndicator<List<RepeatingQuest>>() {
        };
    }

    @Override
    protected Class<RepeatingQuest> getModelClass() {
        return RepeatingQuest.class;
    }

    @Override
    protected String getCollectionName() {
        return "repeatingQuests";
    }

    @Override
    public void findAllNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener) {
        Query query = getCollectionReference().orderByChild("allDay").equalTo(false);
        listenForListChange(query, listener, this::applyActiveRepeatingQuestFilter);
    }

    @Override
    public void listenForAllNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener) {
        Query query = getCollectionReference().orderByChild("allDay").equalTo(false);
        listenForListChange(query, listener, this::applyActiveRepeatingQuestFilter);
    }

    @Override
    public void listenForNonFlexibleNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener) {
        Query query = getCollectionReference().orderByChild("recurrence/flexibleCount").equalTo(0);
        listenForListChange(query, listener, this::applyActiveRepeatingQuestFilter);
    }

    @Override
    public void findByExternalSourceMappingId(String source, String sourceId, OnDataChangedListener<RepeatingQuest> listener) {
        Query query = getCollectionReference().orderByChild("sourceMapping/" + source).equalTo(sourceId);
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
        listenForListChange(query, listener, this::applyActiveRepeatingQuestFilter);
    }

    @Override
    public void findActiveNotForChallenge(String searchText, Challenge challenge, OnDataChangedListener<List<RepeatingQuest>> listener) {
        listenForSingleListChange(getCollectionReference(), listener, data -> data
                .filter(rq -> !challenge.getId().equals(rq.getChallengeId()))
                .filter(rq -> rq.getName().toLowerCase().contains(searchText.toLowerCase()))
                .filter(activeRepeatingQuestFilter())
        );
    }

    @Override
    public void findByChallenge(Challenge challenge, OnDataChangedListener<List<RepeatingQuest>> listener) {
        Query query = getCollectionReference().equalTo(challenge.getId(), "challengeId");
        listenForSingleListChange(query, listener);
    }

    @NonNull
    private Observable<RepeatingQuest> applyActiveRepeatingQuestFilter(Observable<RepeatingQuest> data) {
        return data.filter(activeRepeatingQuestFilter());
    }

    @NonNull
    private Func1<RepeatingQuest, Boolean> activeRepeatingQuestFilter() {
        return rq -> rq.getRecurrence().getDtendDate() == null
                || rq.getRecurrence().getDtendDate().getTime() >= toStartOfDayUTC(LocalDate.now()).getTime();
    }
}
