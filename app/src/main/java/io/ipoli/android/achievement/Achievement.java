package io.ipoli.android.achievement;

import android.support.annotation.StringRes;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public enum Achievement {

    FIRST_QUEST_COMPLETED(1,
            Category.BRONZE,
            R.string.achievemet_first_quest_completed_name,
            R.string.achievemet_first_quest_completed_desc,
            10, 15);

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
