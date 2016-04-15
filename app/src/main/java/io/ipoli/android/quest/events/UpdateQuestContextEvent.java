package io.ipoli.android.quest.events;

import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/16/16.
 */
public class UpdateQuestContextEvent {
    public final Quest quest;
    public final QuestContext questContext;

    public UpdateQuestContextEvent(Quest quest, QuestContext questContext) {
        this.quest = quest;
        this.questContext = questContext;
    }
}
