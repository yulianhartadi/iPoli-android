package io.ipoli.android.quest.persistence;

import android.content.Context;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Reminder;
import io.ipoli.android.quest.data.RepeatingQuest;

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
    public void findAllNonAllDayActiveRepeatingQuests(OnDatabaseChangedListener<List<RepeatingQuest>> listener) {

    }

    @Override
    public void findNonFlexibleNonAllDayActiveRepeatingQuests(OnDatabaseChangedListener<List<RepeatingQuest>> listener) {

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
    public void findActiveForChallenge(Challenge challenge, OnDatabaseChangedListener<List<RepeatingQuest>> listener) {

    }

    @Override
    public List<RepeatingQuest> findActiveNotForChallenge(String query, Challenge challenge) {
        return null;
    }

    @Override
    public List<RepeatingQuest> findNotDeleted(Challenge challenge) {
        return null;
    }
}
