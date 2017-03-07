package io.ipoli.android.challenge.persistence;

import android.util.Pair;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.QuestData;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebaseChallengePersistenceService extends BaseFirebasePersistenceService<Challenge> implements ChallengePersistenceService {

    public FirebaseChallengePersistenceService(Bus eventBus) {
        super(eventBus);
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
    protected DatabaseReference getCollectionReference() {
        return getPlayerReference().child(getCollectionName());
    }

    @Override
    public void listenForAll(OnDataChangedListener<List<Challenge>> listener) {
        Query query = getCollectionReference().orderByChild("end");
        listenForListChange(query, listener, c -> c.getCompletedAtDate() == null);
    }

    @Override
    public void delete(Challenge challenge, boolean deleteWithQuests) {

    }

    @Override
    public void findAllQuestsAndRepeatingQuestsNotForChallenge(String query, Challenge challenge, OnDataChangedListener<Pair<List<RepeatingQuest>, List<Quest>>> listener) {
        
    }

    @Override
    public void delete(Challenge challenge) {
        Map<String, Object> data = new HashMap<>();

        Map<String, QuestData> questIds = challenge.getQuestsData();
        for (String questId : questIds.keySet()) {
            data.put("/quests/" + questId + "/challengeId", null);
            QuestData qd = questIds.get(questId);
            if (qd.getScheduledDate() != null) {
                data.put("/dayQuests/" + qd.getScheduledDate() + "/" + questId + "/challengeId", null);
            } else {
                data.put("/inboxQuests/" + questId + "/challengeId", null);
            }
        }

        Map<String, Boolean> repeatingQuestIds = challenge.getRepeatingQuestIds();
        for (String rqId : repeatingQuestIds.keySet()) {
            data.put("/repeatingQuests/" + rqId + "/challengeId", null);
        }

        data.put("/challenges/" + challenge.getId(), null);

        getPlayerReference().updateChildren(data);
    }
}