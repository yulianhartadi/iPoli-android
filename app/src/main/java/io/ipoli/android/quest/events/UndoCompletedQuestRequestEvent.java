package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/17/16.
 */
public class UndoCompletedQuestRequestEvent {
    public Quest quest;

    public UndoCompletedQuestRequestEvent(Quest quest) {
        this.quest = quest;
    }
}
