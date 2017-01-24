package io.ipoli.android.challenge.events;

import java.util.List;

import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/24/17.
 */
public class NewChallengeQuestsPickedEvent {
    public final List<Quest> quests;
    public final List<RepeatingQuest> repeatingQuests;

    public NewChallengeQuestsPickedEvent(List<Quest> quests, List<RepeatingQuest> repeatingQuests) {
        this.quests = quests;
        this.repeatingQuests = repeatingQuests;
    }
}
