package io.ipoli.android.app.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/17/16.
 */
public class UndoCompletedQuestEvent {
    public Quest quest;

    public UndoCompletedQuestEvent(Quest quest) {
        this.quest = quest;
    }
}
