package com.curiousily.ipoli.quest.services.events;

import com.curiousily.ipoli.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/15.
 */
public class QuestSavedEvent {
    public final Quest quest;

    public QuestSavedEvent(Quest quest) {

        this.quest = quest;
    }
}
