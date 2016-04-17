package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/17/16.
 */
public class QuestDifficultyChangedEvent {
    public final Quest quest;
    public final String difficulty;

    public QuestDifficultyChangedEvent(Quest quest, String difficulty) {
        this.quest = quest;
        this.difficulty = difficulty;
    }
}
