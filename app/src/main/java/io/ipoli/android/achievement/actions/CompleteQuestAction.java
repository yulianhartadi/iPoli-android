package io.ipoli.android.achievement.actions;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/25/17.
 */
public class QuestCompleteAction extends SimpleAchievementAction {
    
    private final Quest quest;

    public QuestCompleteAction(Action action, Quest quest) {
        super(action);
        this.quest = quest;
    }
}
