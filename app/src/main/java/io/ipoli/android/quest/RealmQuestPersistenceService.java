package io.ipoli.android.quest;

import android.content.Context;

import java.util.Date;
import java.util.List;

import io.realm.Realm;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class RealmQuestPersistenceService implements QuestPersistenceService {

    private Realm realm;

    public RealmQuestPersistenceService(Context context) {
        realm = Realm.getInstance(context);
    }

    @Override
    public Quest save(Quest quest) {
        realm.beginTransaction();
        Quest realmQuest = realm.copyToRealm(quest);
        realm.commitTransaction();
        return realmQuest;
    }

    @Override
    public List<Quest> findAllUncompleted() {
        return realm.copyFromRealm(realm.where(Quest.class).notEqualTo("status", Quest.Status.COMPLETED.name()).findAll());
    }

    @Override
    public void update(Quest quest, String status, Date due) {
        realm.beginTransaction();
        Quest q = realm.copyToRealmOrUpdate(quest);
        q.setStatus(status);
        q.setDue(due);
        realm.commitTransaction();
    }
}
