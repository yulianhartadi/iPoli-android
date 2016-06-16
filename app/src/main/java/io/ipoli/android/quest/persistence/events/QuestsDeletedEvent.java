package io.ipoli.android.quest.persistence.events;

import java.util.List;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/16/16.
 */
public class QuestsDeletedEvent {
    public List<Quest> quests;

    public QuestsDeletedEvent(List<Quest> quests) {
        this.quests = quests;
    }
}
