package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/16/16.
 */
public class QuestDurationUpdatedEvent {
    public final Quest quest;
    public final String duration;

    public QuestDurationUpdatedEvent(Quest quest, String duration) {
        this.quest = quest;
        this.duration = duration;
    }
}
