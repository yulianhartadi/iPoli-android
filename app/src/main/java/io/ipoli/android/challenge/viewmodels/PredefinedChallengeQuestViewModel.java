package io.ipoli.android.challenge.viewmodels;

import io.ipoli.android.quest.data.BaseQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/20/16.
 */
public class PredefinedChallengeQuestViewModel {

    private boolean isSelected;
    private final String name;
    private final BaseQuest quest;

    public PredefinedChallengeQuestViewModel(String name, BaseQuest quest) {
        this(name, quest, true);
    }

    public PredefinedChallengeQuestViewModel(BaseQuest quest, boolean isSelected) {
        this(quest.getName(), quest, isSelected);
    }

    public PredefinedChallengeQuestViewModel(String name, BaseQuest quest, boolean isSelected) {
        this.name = name;
        this.quest = quest;
        this.isSelected = isSelected;
    }

    public PredefinedChallengeQuestViewModel(BaseQuest quest) {
        this(quest, true);
    }


    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void select() {
        isSelected = true;
    }

    public void deselect() {
        isSelected = false;
    }

    public BaseQuest getQuest() {
        return quest;
    }
}
