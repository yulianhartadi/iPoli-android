package io.ipoli.android.quest.events;

import io.ipoli.android.quest.QuestContext;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/16/16.
 */
public class NewQuestContextChangedEvent {
    public final QuestContext questContext;

    public NewQuestContextChangedEvent(QuestContext questContext) {
        this.questContext = questContext;
    }
}
