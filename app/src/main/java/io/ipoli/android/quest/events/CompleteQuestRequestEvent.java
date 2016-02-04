package io.ipoli.android.quest.events;

import io.ipoli.android.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class CompleteQuestRequestEvent {
    public final Quest quest;

    public CompleteQuestRequestEvent(Quest quest) {
        this.quest = quest;
    }
}
