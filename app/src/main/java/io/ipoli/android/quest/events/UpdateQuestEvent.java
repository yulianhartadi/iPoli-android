package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/16/16.
 */
public class UpdateQuestEvent {
    public final Quest quest;

    public UpdateQuestEvent(Quest quest) {

        this.quest = quest;
    }
}
