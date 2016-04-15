package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/15/16.
 */
public class DeleteQuestRequestedEvent {
    public final Quest quest;
    public final String source;

    public DeleteQuestRequestedEvent(Quest quest, String source) {
        this.quest = quest;
        this.source = source;
    }
}
