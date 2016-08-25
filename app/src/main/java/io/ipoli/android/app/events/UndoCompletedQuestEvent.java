package io.ipoli.android.app.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/17/16.
 */
public class UndoCompletedQuestEvent {
    public final Quest quest;
    public final long experience;
    public final long coins;

    public UndoCompletedQuestEvent(Quest quest, long experience, long coins) {
        this.quest = quest;
        this.experience = experience;
        this.coins = coins;
    }
}
