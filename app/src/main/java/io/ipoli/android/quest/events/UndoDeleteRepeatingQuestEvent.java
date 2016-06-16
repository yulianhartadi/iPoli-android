package io.ipoli.android.quest.events;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/9/16.
 */
public class UndoDeleteRepeatingQuestEvent {
    public final RepeatingQuest repeatingQuest;
    public final EventSource source;

    public UndoDeleteRepeatingQuestEvent(RepeatingQuest repeatingQuest, EventSource source) {
        this.repeatingQuest = repeatingQuest;
        this.source = source;
    }
}
