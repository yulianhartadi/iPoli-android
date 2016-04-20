package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/15/16.
 */
public class QuestDraggedEvent {
    public final Quest quest;

    public QuestDraggedEvent(Quest quest) {
        this.quest = quest;
    }
}
