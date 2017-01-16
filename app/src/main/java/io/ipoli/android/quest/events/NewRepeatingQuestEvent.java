package io.ipoli.android.quest.events;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/7/16.
 */
public class NewRepeatingQuestEvent {
    public final RepeatingQuest repeatingQuest;

    public NewRepeatingQuestEvent(RepeatingQuest repeatingQuest, EventSource eventSource) {
        this.repeatingQuest = repeatingQuest;
    }
}