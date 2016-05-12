package io.ipoli.android.quest.persistence;

import java.util.List;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.quest.data.Habit;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/31/16.
 */
public interface HabitPersistenceService extends PersistenceService<Habit> {

    Observable<List<Habit>> findAllHabits();

    void delete(Habit habit);

    void deleteByExternalSourceMappingId(String source, String sourceId);

    Observable<Habit> findByExternalSourceMappingId(String source, String sourceId);
}
