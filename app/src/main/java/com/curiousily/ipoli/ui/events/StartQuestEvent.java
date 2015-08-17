package com.curiousily.ipoli.ui.events;

import com.curiousily.ipoli.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/17/15.
 */
public class StartQuestEvent {
    public final Quest quest;

    public StartQuestEvent(Quest quest) {
        this.quest = quest;
    }
}
