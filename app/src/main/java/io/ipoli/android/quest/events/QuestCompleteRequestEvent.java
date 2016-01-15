package io.ipoli.android.quest.events;

import io.ipoli.android.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class QuestCompleteRequestEvent {
    public final Quest quest;
    public final int position;

    public QuestCompleteRequestEvent(Quest quest, int position) {
        this.quest = quest;
        this.position = position;
    }
}
