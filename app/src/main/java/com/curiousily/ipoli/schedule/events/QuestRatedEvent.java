package com.curiousily.ipoli.schedule.events;

import com.curiousily.ipoli.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/24/15.
 */
public class QuestRatedEvent {
    public final Quest quest;

    public QuestRatedEvent(Quest quest) {
        this.quest = quest;
    }
}
