package com.curiousily.ipoli.quest.events;

import com.curiousily.ipoli.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/19/15.
 */
public class BuildQuestEvent {
    public final Quest quest;

    public BuildQuestEvent(Quest quest) {
        this.quest = quest;
    }
}
