package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class NewQuestEvent {
    public final Quest quest;

    public NewQuestEvent(Quest quest) {
        this.quest = quest;
    }
}
