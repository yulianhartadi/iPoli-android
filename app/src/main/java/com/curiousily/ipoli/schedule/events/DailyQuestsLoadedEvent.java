package com.curiousily.ipoli.schedule.events;

import com.curiousily.ipoli.quest.Quest;

import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/16/15.
 */
public class DailyQuestsLoadedEvent {

    public final List<Quest> quests;

    public DailyQuestsLoadedEvent(List<Quest> quests) {
        this.quests = quests;
    }
}
