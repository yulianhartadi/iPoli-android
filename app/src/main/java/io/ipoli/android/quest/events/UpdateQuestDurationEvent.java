package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/16/16.
 */
public class UpdateQuestDurationEvent {
    public final Quest quest;

    public UpdateQuestDurationEvent(Quest quest) {
        this.quest = quest;
    }
}
