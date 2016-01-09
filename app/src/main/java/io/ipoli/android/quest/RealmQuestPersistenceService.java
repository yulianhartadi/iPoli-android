package io.ipoli.android.quest;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.ipoli.android.utils.DateUtils;
import io.realm.Realm;
import io.realm.Sort;

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
        Quest realmQuest = realm.copyToRealmOrUpdate(quest);
        realm.commitTransaction();
        return realmQuest;
    }

    @Override
    public List<Quest> saveAll(List<Quest> quests) {
        realm.beginTransaction();
        List<Quest> realmQuests = realm.copyToRealmOrUpdate(quests);
        realm.commitTransaction();
        return realmQuests;
    }

    @Override
    public List<Quest> findAllUncompleted() {
        return realm.copyFromRealm(realm.where(Quest.class)
                .notEqualTo("status", Quest.Status.COMPLETED.name())
                .findAllSorted("createdAt", Sort.DESCENDING));
    }

    @Override
    public List<Quest> findAllForToday() {
        Calendar yesterday = DateUtils.getTodayAtMidnight();
        yesterday.add(Calendar.SECOND, -1);

        Calendar tomorrow = DateUtils.getTodayAtMidnight();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        return realm.copyFromRealm(realm.where(Quest.class)
                .greaterThan("due", yesterday.getTime())
                .lessThan("due", tomorrow.getTime())
                .equalTo("status", Quest.Status.PLANNED.name())
                .findAllSorted("order", Sort.ASCENDING));
    }

}
