package io.ipoli.android.app.services.readers;

import io.ipoli.android.quest.data.Habit;
import io.ipoli.android.quest.persistence.HabitPersistenceService;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class RealmHabitListReader implements ListReader<Habit> {

    private final HabitPersistenceService habitPersistenceService;

    public RealmHabitListReader(HabitPersistenceService habitPersistenceService) {
        this.habitPersistenceService = habitPersistenceService;
    }

    public Observable<Habit> read() {
        return habitPersistenceService.findAllWhoNeedSyncWithRemote().concatMapIterable(habits -> habits);
    }
}