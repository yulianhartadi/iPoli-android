package io.ipoli.android.quest.persistence;

import java.util.List;
import java.util.Map;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/31/16.
 */
public interface RepeatingQuestPersistenceService extends PersistenceService<RepeatingQuest> {

    void findAllNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener);

    void listenForAllNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener);

    void listenForNonFlexibleNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener);

    void saveWithQuests(Map<RepeatingQuest, List<Quest>> repeatingQuestToScheduledQuests);

    void saveWithQuests(RepeatingQuest repeatingQuest, List<Quest> quests);

    void update(RepeatingQuest repeatingQuest, List<Quest> questsToRemove, List<Quest> questsToCreate);

    void removeFromChallenge(RepeatingQuest repeatingQuest);

    void addToChallenge(List<RepeatingQuest> repeatingQuests, String challengeId);
}
