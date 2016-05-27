package io.ipoli.android.quest.persistence;

import android.text.TextUtils;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.RepeatingQuestSavedEvent;
import io.ipoli.android.quest.persistence.events.RepeatingQuestDeletedEvent;
import io.realm.Realm;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/31/16.
 */
public class RealmRepeatingQuestPersistenceService extends BaseRealmPersistenceService<RepeatingQuest> implements RepeatingQuestPersistenceService {

    private final Bus eventBus;

    public RealmRepeatingQuestPersistenceService(Bus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    protected Class<RepeatingQuest> getRealmObjectClass() {
        return RepeatingQuest.class;
    }

    @Override
    protected void onObjectSaved(RepeatingQuest object) {
        eventBus.post(new RepeatingQuestSavedEvent(object));
    }

    @Override
    protected void onObjectDeleted(String id) {
        eventBus.post(new RepeatingQuestDeletedEvent(id));
    }

    @Override
    public Observable<List<RepeatingQuest>> findAllNonAllDayRepeatingQuests() {
        return findAll(where -> where.isNotNull("name").equalTo("allDay", false).isNotNull("recurrence.rrule").findAllAsync());
    }

    @Override
    public Observable<String> deleteBySourceMappingId(String source, String sourceId) {

        if (TextUtils.isEmpty(source) || TextUtils.isEmpty(sourceId)) {
            return Observable.empty();
        }

        Realm realm = getRealm();

        return find(where -> where.equalTo("sourceMapping." + source, sourceId).findFirstAsync()).flatMap(realmRepeatingQuest -> {
            if (realmRepeatingQuest == null) {
                realm.close();
                return Observable.empty();
            }

            final String repeatingQuestId = realmRepeatingQuest.getId();

            return Observable.create(subscriber -> {
                realm.executeTransactionAsync(backgroundRealm -> {
                    RepeatingQuest repeatingQuestToDelete = backgroundRealm.where(getRealmObjectClass())
                            .equalTo("sourceMapping." + source, sourceId)
                            .findFirst();
                    repeatingQuestToDelete.deleteFromRealm();
                }, () -> {
                    subscriber.onNext(repeatingQuestId);
                    subscriber.onCompleted();
                    onObjectDeleted(repeatingQuestId);
                    realm.close();
                }, error -> {
                    subscriber.onError(error);
                    realm.close();
                });
            });
        });
    }

    @Override
    public RepeatingQuest findByExternalSourceMappingIdSync(String source, String sourceId) {
        try (Realm realm = getRealm()) {
            return realm.copyFromRealm(realm.where(getRealmObjectClass())
                    .equalTo("sourceMapping." + source, sourceId)
                    .findFirst());
        }
    }
}
