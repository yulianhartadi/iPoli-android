package io.ipoli.android.achievement;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public class AchievementsProgress {

    private int completeQuestCount;

    public int getCompleteQuestCount() {
        return completeQuestCount;
    }

    @JsonIgnore
    public void incrementQuestCompleteCount() {
        completeQuestCount++;
    }
}
