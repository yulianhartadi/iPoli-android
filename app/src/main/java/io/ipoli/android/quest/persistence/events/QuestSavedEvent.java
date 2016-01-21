package io.ipoli.android.quest.persistence.events;

import io.ipoli.android.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/21/16.
 */
public class QuestSavedEvent {
    public final Quest quest;

    public QuestSavedEvent(Quest quest) {
        this.quest = quest;
    }
}
