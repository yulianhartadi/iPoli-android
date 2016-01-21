package io.ipoli.android.quest.persistence.events;

import java.util.List;

import io.ipoli.android.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/21/16.
 */
public class QuestsSavedEvent {
    public final List<Quest> quests;

    public QuestsSavedEvent(List<Quest> quests) {
        this.quests = quests;
    }
}
