package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/7/16.
 */
public class RepeatingQuestSavedEvent {
    public final RepeatingQuest repeatingQuest;

    public RepeatingQuestSavedEvent(RepeatingQuest repeatingQuest) {
        this.repeatingQuest = repeatingQuest;
    }
}
