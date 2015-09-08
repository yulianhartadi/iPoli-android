package com.curiousily.ipoli.schedule.ui.events;

import com.curiousily.ipoli.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/8/15.
 */
public class QuestPostponedEvent {
    public final Quest quest;

    public QuestPostponedEvent(Quest quest) {
        this.quest = quest;
    }
}
