package com.curiousily.ipoli.quest.services.events;

import com.curiousily.ipoli.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/11/15.
 */
public class UpdateQuestEvent {
    public final Quest quest;

    public UpdateQuestEvent(Quest quest) {
        this.quest = quest;
    }
}
