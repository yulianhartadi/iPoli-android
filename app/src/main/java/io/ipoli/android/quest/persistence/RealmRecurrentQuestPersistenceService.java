package io.ipoli.android.quest.persistence;

import android.text.TextUtils;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.quest.data.RecurrentQuest;
import io.ipoli.android.quest.events.RecurrentQuestSavedEvent;
import io.ipoli.android.quest.persistence.events.RecurrentQuestDeletedEvent;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/31/16.
 */
public class RealmRecurrentQuestPersistenceService extends BaseRealmPersistenceService<RecurrentQuest> implements RecurrentQuestPersistenceService {

    private final Bus eventBus;

    public RealmRecurrentQuestPersistenceService(Bus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    protected void onObjectSaved(RecurrentQuest obj) {
        eventBus.post(new RecurrentQuestSavedEvent(obj));
    }

    @Override
    protected Class<RecurrentQuest> getRealmObjectClass() {
        return RecurrentQuest.class;
    }

    @Override
    public Observable<List<RecurrentQuest>> findAllHabits() {
        return fromRealm(where().isNotNull("name").isNotNull("recurrence.rrule").findAll());
    }

    @Override
    public void delete(RecurrentQuest recurrentQuest) {
        if (recurrentQuest == null) {
            return;
        }
        String id = recurrentQuest.getId();
        getRealm().beginTransaction();
        RecurrentQuest realmQuest = where()
                .equalTo("id", id)
                .findFirst();
        if (realmQuest == null) {
            getRealm().cancelTransaction();
            return;
        }
        realmQuest.removeFromRealm();
        getRealm().commitTransaction();
        eventBus.post(new RecurrentQuestDeletedEvent(id));
    }

    @Override
    public void deleteByExternalSourceMappingId(String source, String sourceId) {
        if(TextUtils.isEmpty(source) || TextUtils.isEmpty(sourceId)) {
            return;
        }
        getRealm().beginTransaction();
        RecurrentQuest realmQuest = where()
                .equalTo("externalSourceMapping." + source, sourceId)
                .findFirst();
        if (realmQuest == null) {
            getRealm().cancelTransaction();
            return;
        }
        realmQuest.removeFromRealm();
        getRealm().commitTransaction();
        eventBus.post(new RecurrentQuestDeletedEvent(realmQuest.getId()));
    }

    @Override
    public Observable<RecurrentQuest> findByExternalSourceMappingId(String source, String sourceId) {
        return fromRealm(where().equalTo("externalSourceMapping." + source, sourceId).findFirst());
    }
}
