package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class QuestUpdatedEvent {
    public final Quest quest;

    public QuestUpdatedEvent(Quest quest) {
        this.quest = quest;
    }
}
