package io.ipoli.android.achievement.actions;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/25/17.
 */
public class CompleteQuestAction extends SimpleAchievementAction {

    public final Quest quest;

    public CompleteQuestAction(Quest quest) {
        super(Action.COMPLETE_QUEST);
        this.quest = quest;
    }
}
