package io.ipoli.android.quest.persistence;

import android.text.TextUtils;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.quest.data.Habit;
import io.ipoli.android.quest.events.HabitSavedEvent;
import io.ipoli.android.quest.persistence.events.HabitDeletedEvent;
import io.realm.Realm;
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
    protected Class<Habit> getRealmObjectClass() {
        return Habit.class;
    }

    @Override
    protected void onObjectSaved(Habit object) {
        eventBus.post(new HabitSavedEvent(object));
    }

    @Override
    protected void onObjectDeleted(String id) {
        eventBus.post(new HabitDeletedEvent(id));
    }

    @Override
    public Observable<List<Habit>> findAllNonAllDayHabits() {
        return findAll(where -> where.isNotNull("name").equalTo("allDay", false).isNotNull("recurrence.rrule").findAllAsync());
    }

    @Override
    public Observable<String> deleteBySourceMappingId(String source, String sourceId) {

        if (TextUtils.isEmpty(source) || TextUtils.isEmpty(sourceId)) {
            return Observable.empty();
        }

        Realm realm = getRealm();

        return find(where -> where.equalTo("sourceMapping." + source, sourceId).findFirstAsync()).flatMap(realmHabit -> {
            if (realmHabit == null) {
                realm.close();
                return Observable.empty();
            }

            final String habitId = realmHabit.getId();

            return Observable.create(subscriber -> {
                realm.executeTransactionAsync(backgroundRealm -> {
                    Habit habitToDelete = backgroundRealm.where(getRealmObjectClass())
                            .equalTo("sourceMapping." + source, sourceId)
                            .findFirst();
                    habitToDelete.deleteFromRealm();
                }, () -> {
                    subscriber.onNext(habitId);
                    subscriber.onCompleted();
                    onObjectDeleted(habitId);
                    realm.close();
                }, error -> {
                    subscriber.onError(error);
                    realm.close();
                });
            });
        });
    }

    @Override
    public Observable<Habit> findByExternalSourceMappingId(String source, String sourceId) {
        return find(where -> where.equalTo("sourceMapping." + source, sourceId).findFirstAsync());
    }
}
