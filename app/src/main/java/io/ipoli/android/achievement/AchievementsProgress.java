package io.ipoli.android.achievement;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.ipoli.android.app.persistence.PersistedObject;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public class AchievementsProgress extends PersistedObject {

    private int completeQuestCount;

    public AchievementsProgress(String type) {
        super(type);
    }

    public int getCompleteQuestCount() {
        return completeQuestCount;
    }

    @JsonIgnore
    public void incrementQuestCompleteCount() {
        completeQuestCount++;
    }
}
