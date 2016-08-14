package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/12/16.
 */
public class StartQuestRequestEvent {
    public final Quest quest;

    public StartQuestRequestEvent(Quest quest) {
        this.quest = quest;
    }
}
