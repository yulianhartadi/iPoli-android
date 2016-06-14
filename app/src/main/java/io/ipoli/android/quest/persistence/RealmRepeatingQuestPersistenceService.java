package io.ipoli.android.quest.persistence;

import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.RepeatingQuestSavedEvent;
import io.realm.Realm;

import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/31/16.
 */
public class RealmRepeatingQuestPersistenceService extends BaseRealmPersistenceService<RepeatingQuest> implements RepeatingQuestPersistenceService {

    private final Bus eventBus;

    public RealmRepeatingQuestPersistenceService(Bus eventBus, Realm realm) {
        super(realm);
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
    public List<RepeatingQuest> findAllNonAllDayActiveRepeatingQuests() {
        return findAll(where -> where.isNotNull("name")
                .equalTo("allDay", false)
                .isNotNull("recurrence.rrule")
                .beginGroup()
                .isNull("recurrence.dtend")
                .or()
                .greaterThan("recurrence.dtend", toStartOfDayUTC(LocalDate.now()))
                .endGroup()
                .findAll());
    }

    @Override
    public void findAllNonAllDayActiveRepeatingQuests(OnDatabaseChangedListener<RepeatingQuest> listener) {
        listenForChanges(where().isNotNull("name")
                .equalTo("allDay", false)
                .isNotNull("recurrence.rrule")
                .beginGroup()
                .isNull("recurrence.dtend")
                .or()
                .greaterThan("recurrence.dtend", toStartOfDayUTC(LocalDate.now()))
                .endGroup()
                .findAllAsync(), listener);
    }

    @Override
    public RepeatingQuest findByExternalSourceMappingIdSync(String source, String sourceId) {
        Realm realm = getRealm();
        RepeatingQuest repeatingQuest = realm.where(getRealmObjectClass())
                .equalTo("sourceMapping." + source, sourceId)
                .findFirst();
        if (repeatingQuest == null) {
            return null;
        }
        return realm.copyFromRealm(repeatingQuest);
    }
}
