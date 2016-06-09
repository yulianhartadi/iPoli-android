package io.ipoli.android.quest.persistence;

import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.app.utils.DateUtils;
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
    public Observable<List<RepeatingQuest>> findAllNonAllDayActiveRepeatingQuests() {
        return findAll(where -> where.isNotNull("name")
                .equalTo("allDay", false)
                .isNotNull("recurrence.rrule")
                .beginGroup()
                .isNull("recurrence.dtend")
                .or()
                .greaterThan("recurrence.dtend", DateUtils.toStartOfDayUTC(LocalDate.now()))
                .endGroup()
                .findAllAsync());
    }

    @Override
    public RepeatingQuest findByExternalSourceMappingIdSync(String source, String sourceId) {
        try (Realm realm = getRealm()) {
            RepeatingQuest repeatingQuest = realm.where(getRealmObjectClass())
                    .equalTo("sourceMapping." + source, sourceId)
                    .findFirst();
            if(repeatingQuest == null) {
                return null;
            }
            return realm.copyFromRealm(repeatingQuest);
        }
    }

    @Override
    public Observable<RepeatingQuest> findByExternalSourceMappingId(String source, String sourceId) {
        return find(where -> where.equalTo("sourceMapping." + source, sourceId).findFirstAsync());
    }
}
