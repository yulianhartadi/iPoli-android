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
            R.string.achievement_first_quest_completed_name,
            R.string.achievement_first_quest_completed_desc,
            0, 0),
    FIRST_REPEATING_QUEST_CREATED(8,
            Category.BRONZE,
            R.string.achievement_first_repeating_quest_created_name,
            R.string.achievement_first_repeating_quest_created_desc,
            0, 0),
    FIRST_CHALLENGE_CREATED(9,
            Category.BRONZE,
            R.string.achievement_first_challenge_created_name,
            R.string.achievement_first_challenge_created_desc,
            0, 0),
    FIRST_REWARD_USED(13,
            Category.BRONZE,
            R.string.achievement_first_reward_used_name,
            R.string.achievement_first_reward_used_desc,
            0, 0),
    FIRST_DAILY_CHALLENGE_COMPLETED(10,
            Category.BRONZE,
            R.string.achievement_first_daily_challenge_completed_name,
            R.string.achievement_first_daily_challenge_completed_desc,
            0, 0),
    FIRST_POST_CREATED(11,
            Category.BRONZE,
            R.string.achievement_first_post_created_name,
            R.string.achievement_first_post_created_desc,
            0, 0),
    FIRST_AVATAR_CHANGED(12,
            Category.BRONZE,
            R.string.achievement_first_avatar_changed_name,
            R.string.achievement_first_avatar_changed_desc,
            0, 0),
    FIRST_CHALLENGE_COMPLETED(23,
            Category.SILVER,
            R.string.achievement_first_challenge_completed_name,
            R.string.achievement_first_challenge_completed_desc,
            15, 100),
    FIVE_POSTS_CREATED(14,
            Category.SILVER,
            R.string.achievement_five_posts_created_name,
            R.string.achievement_five_posts_created_desc,
            15, 100),
    COMPLETE_10_QUESTS_IN_A_DAY(2,
            Category.SILVER,
            R.string.achievement_complete_10_quests_in_a_day_name,
            R.string.achievement_complete_10_quests_in_a_day_desc,
            15, 100),
    COMPLETE_DAILY_CHALLENGE_FOR_5_DAYS_IN_A_ROW(5,
            Category.SILVER,
            R.string.achievement_complete_daily_challenge_5_days_in_row_name,
            R.string.achievement_complete_daily_challenge_5_days_in_row_desc,
            15, 100),
    LEVEL_15TH(6,
            Category.SILVER,
            R.string.achievement_reach_level_15th_name,
            R.string.achievement_reach_level_15th_desc,
            15, 100),
    FIRST_POWER_UP(15,
            Category.SILVER,
            R.string.achievement_first_power_up_name,
            R.string.achievement_first_power_up_desc,
            15, 100),
    //? on app start
    HAVE_1K_COINS(16,
            Category.SILVER,
            R.string.achievement_have_1k_coins_name,
            R.string.achievement_have_1k_coins_desc,
            15, 100),
    INVITE_FRIEND(17,
            Category.SILVER,
            R.string.achievement_invite_friend_name,
            R.string.achievement_invite_friend_desc,
            15, 100),
    CHANGE_PET(18,
            Category.SILVER,
            R.string.achievement_change_pet_name,
            R.string.achievement_change_pet_desc,
            15, 100),
    PET_DIED(19,
            Category.SILVER,
            R.string.achievement_first_pet_died_name,
            R.string.achievement_first_pet_died_desc,
            15, 100),
    FIRST_FOLLOW(20,
            Category.SILVER,
            R.string.achievement_first_follow_name,
            R.string.achievement_first_follow_desc,
            15, 100),
    // on app start ?
    FIRST_FOLLOWER(21,
            Category.SILVER,
            R.string.achievement_first_follower_name,
            R.string.achievement_first_follower_desc,
            15, 100),
    LEVEL_20TH(7,
            Category.GOLD,
            R.string.achievement_reach_level_20th_name,
            R.string.achievement_reach_level_20th_desc,
            100, 300),
    GAIN_500_XP_IN_A_DAY(3,
            Category.GOLD,
            R.string.achievement_gain_xp_in_a_day_name,
            R.string.achievement_gain_xp_in_a_day_desc,
            100, 300),
    COMPLETE_QUEST_FOR_100_DAYS_IN_A_ROW(4,
            Category.GOLD,
            R.string.achievement_complete_quests_in_row_name,
            R.string.achievement_complete_quests_in_row_desc,
            100, 300),
    FEEDBACK_SENT(22,
            Category.GOLD,
            R.string.achievement_send_feedback_name,
            R.string.achievement_send_feedback_desc,
            100, 300);

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
