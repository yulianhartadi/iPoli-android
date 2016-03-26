package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/11/16.
 */
public class DeleteQuestEvent {
    public final Quest quest;

    public DeleteQuestEvent(Quest quest) {
        this.quest = quest;
    }
}
