package io.ipoli.android.quest.persistence;

import java.util.List;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.quest.data.RepeatingQuest;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/31/16.
 */
public interface RepeatingQuestPersistenceService extends PersistenceService<RepeatingQuest> {

    Observable<List<RepeatingQuest>> findAllNonAllDayRepeatingQuests();

    Observable<String> deleteBySourceMappingId(String source, String sourceId);

    RepeatingQuest findByExternalSourceMappingIdSync(String source, String sourceId);
}
