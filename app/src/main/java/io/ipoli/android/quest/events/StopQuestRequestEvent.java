package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/12/16.
 */
public class StopQuestRequestEvent {
    public final Quest quest;

    public StopQuestRequestEvent(Quest quest) {
        this.quest = quest;
    }
}
