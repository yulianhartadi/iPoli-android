package io.ipoli.android.quest.events;

import io.ipoli.android.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public class ShowQuestEvent {
    public final Quest quest;

    public ShowQuestEvent(Quest quest) {
        this.quest = quest;
    }
}
