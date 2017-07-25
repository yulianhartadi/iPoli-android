package io.ipoli.android.achievement;

import android.support.annotation.StringRes;

import io.ipoli.android.R;

import static io.ipoli.android.R.string.achievemet_first_quest_completed_name;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public enum Achievement {

    FIRST_QUEST_COMPLETED(1,
            Category.BRONZE,
            achievemet_first_quest_completed_name,
            R.string.achievemet_first_quest_completed_desc,
            0, 0),
    COMPLETE_10_QUESTS_IN_A_DAY(2,
            Category.SILVER,
            achievemet_first_quest_completed_name,
            achievemet_first_quest_completed_name,
            15, 15);

    public final int code;

    public final Achievement.Category category;

    @StringRes
    public final int name;

    @StringRes
    public final int description;

    public final int coins;

    public final int experience;

    public enum Category {
        BRONZE, SILVER, GOLD;
    }

    Achievement(int code, Category category, int name, int description, int coins, int experience) {
        this.code = code;
        this.category = category;
        this.name = name;
        this.description = description;
        this.coins = coins;
        this.experience = experience;
    }

    public static Achievement get(int code) {
        for (Achievement achievement : values()) {
            if (achievement.code == code) {
                return achievement;
            }
        }
        return null;
    }
}
