package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/15/16.
 */
public class UnscheduledQuestDraggedEvent {
    public final Quest quest;

    public UnscheduledQuestDraggedEvent(Quest quest) {
        this.quest = quest;
    }
}
