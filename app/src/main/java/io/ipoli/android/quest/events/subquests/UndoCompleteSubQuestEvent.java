package io.ipoli.android.quest.events.subquests;

import io.ipoli.android.quest.data.SubQuest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/11/16.
 */
public class UndoCompleteSubQuestEvent {
    public final SubQuest subQuest;

    public UndoCompleteSubQuestEvent(SubQuest subQuest) {
        this.subQuest = subQuest;
    }
}
