package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/9/16.
 */
public class UndoDeleteRepeatingQuestEvent {
    public final RepeatingQuest repeatingQuest;

    public UndoDeleteRepeatingQuestEvent(RepeatingQuest repeatingQuest) {
        this.repeatingQuest = repeatingQuest;
    }
}
