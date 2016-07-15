package io.ipoli.android.challenge.events;

import io.ipoli.android.quest.data.BaseQuest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/15/16.
 */
public class RemoveBaseQuestFromChallengeEvent {
    public final BaseQuest baseQuest;

    public RemoveBaseQuestFromChallengeEvent(BaseQuest baseQuest) {
        this.baseQuest = baseQuest;
    }
}
