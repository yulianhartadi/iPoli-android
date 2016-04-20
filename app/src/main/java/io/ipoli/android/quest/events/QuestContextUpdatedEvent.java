package io.ipoli.android.quest.events;

import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/16/16.
 */
public class QuestContextUpdatedEvent {
    public final Quest quest;
    public final QuestContext questContext;

    public QuestContextUpdatedEvent(Quest quest, QuestContext questContext) {
        this.quest = quest;
        this.questContext = questContext;
    }
}
