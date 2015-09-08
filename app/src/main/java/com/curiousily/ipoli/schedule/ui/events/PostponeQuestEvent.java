package com.curiousily.ipoli.schedule.ui.events;

import com.curiousily.ipoli.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/8/15.
 */
public class PostponeQuestEvent {
    public final Quest quest;

    public PostponeQuestEvent(Quest quest) {
        this.quest = quest;
    }
}
