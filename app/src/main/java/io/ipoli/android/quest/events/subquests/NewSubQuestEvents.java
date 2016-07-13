package io.ipoli.android.quest.events.subquests;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.data.SubQuest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/11/16.
 */
public class NewSubQuestEvents {
    public final SubQuest subQuest;
    public final EventSource source;

    public NewSubQuestEvents(SubQuest subQuest, EventSource source) {
        this.subQuest = subQuest;
        this.source = source;
    }
}
