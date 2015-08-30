package com.curiousily.ipoli.schedule.ui.events;

import com.curiousily.ipoli.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/31/15.
 */
public class ShowQuestEvent {
    public final Quest quest;

    public ShowQuestEvent(Quest quest) {
        this.quest = quest;
    }
}
