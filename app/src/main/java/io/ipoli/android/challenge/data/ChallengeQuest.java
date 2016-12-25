package io.ipoli.android.challenge.data;

import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/25/16.
 */
public class ChallengeQuest {

    private String questId;
    private String name;
    private String category;
    private boolean isRepeating;

    public ChallengeQuest() {

    }

    public ChallengeQuest(Quest quest) {
        isRepeating = false;
    }

    public ChallengeQuest(RepeatingQuest repeatingQuest) {
        isRepeating = true;
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
    }
}
