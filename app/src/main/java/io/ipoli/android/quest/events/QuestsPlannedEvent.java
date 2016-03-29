package io.ipoli.android.quest.events;

import java.util.List;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class QuestsPlannedEvent {
    public final List<Quest> quests;

    public QuestsPlannedEvent(List<Quest> quests) {
        this.quests = quests;
    }
}
