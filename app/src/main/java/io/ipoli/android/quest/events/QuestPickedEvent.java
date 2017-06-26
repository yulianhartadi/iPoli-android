package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/26/17.
 */

public class QuestPickedEvent {
    public final Quest quest;

    public QuestPickedEvent(Quest quest) {
        this.quest = quest;
    }
}
