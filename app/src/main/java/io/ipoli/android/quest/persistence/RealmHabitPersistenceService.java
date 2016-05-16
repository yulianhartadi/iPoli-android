package io.ipoli.android.quest.persistence;

import android.text.TextUtils;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.quest.data.Habit;
import io.ipoli.android.quest.events.HabitSavedEvent;
import io.ipoli.android.quest.persistence.events.HabitDeletedEvent;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/31/16.
 */
public class RealmHabitPersistenceService extends BaseRealmPersistenceService<Habit> implements HabitPersistenceService {

    private final Bus eventBus;

    public RealmHabitPersistenceService(Bus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    protected void onObjectSaved(Habit obj) {
        eventBus.post(new HabitSavedEvent(obj));
    }

    @Override
    protected Class<Habit> getRealmObjectClass() {
        return Habit.class;
    }

    @Override
    public Observable<List<Habit>> findAllNonAllDayHabits() {
        return fromRealm(where().isNotNull("name").equalTo("allDay", false).isNotNull("recurrence.rrule").findAll());
    }

    @Override
    public void delete(Habit habit) {
        if (habit == null) {
            return;
        }
        String id = habit.getId();
        getRealm().beginTransaction();
        Habit realmQuest = where()
                .equalTo("id", id)
                .findFirst();
        if (realmQuest == null) {
            getRealm().cancelTransaction();
            return;
        }
        realmQuest.deleteFromRealm();
        getRealm().commitTransaction();
        eventBus.post(new HabitDeletedEvent(id));
    }

    @Override
    public void deleteBySourceMappingId(String source, String sourceId) {
        if (TextUtils.isEmpty(source) || TextUtils.isEmpty(sourceId)) {
            return;
        }
        getRealm().beginTransaction();
        Habit realmQuest = where()
                .equalTo("sourceMapping." + source, sourceId)
                .findFirst();
        if (realmQuest == null) {
            getRealm().cancelTransaction();
            return;
        }
        realmQuest.deleteFromRealm();
        getRealm().commitTransaction();
        eventBus.post(new HabitDeletedEvent(realmQuest.getId()));
    }

    @Override
    public Observable<Habit> findByExternalSourceMappingId(String source, String sourceId) {
        return fromRealm(where().equalTo("sourceMapping." + source, sourceId).findFirst());
    }
}
