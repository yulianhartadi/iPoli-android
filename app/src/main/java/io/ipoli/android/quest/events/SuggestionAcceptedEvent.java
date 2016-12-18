package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/27/16.
 */
public class SuggestionAcceptedEvent {

    public final Quest quest;
    public final int startMinute;

    public SuggestionAcceptedEvent(Quest quest, int startMinute) {
        this.quest = quest;
        this.startMinute = startMinute;
    }
}
