package io.ipoli.android.achievement

enum class Achievement(val category: Category, val coins: Int, val experience: Int) {
    FIRST_QUEST_COMPLETED(
        Category.BRONZE,
        0, 0
    ),
    FIRST_REPEATING_QUEST_CREATED(
        Category.BRONZE,
        0, 0
    ),
    FIRST_CHALLENGE_CREATED(
        Category.BRONZE,
        0, 0
    ),
    FIRST_REWARD_USED(
        Category.BRONZE,
        0, 0
    ),
    FIRST_DAILY_CHALLENGE_COMPLETED(
        Category.BRONZE,
        0, 0
    ),
    FIRST_POST_CREATED(
        Category.BRONZE,
        0, 0
    ),
    FIRST_AVATAR_CHANGED(
        Category.BRONZE,
        0, 0
    ),
    FIRST_CHALLENGE_COMPLETED(
        Category.SILVER,
        15, 100
    ),
    FIVE_POSTS_CREATED(
        Category.SILVER,
        15, 100
    ),
    COMPLETE_10_QUESTS_IN_A_DAY(
        Category.SILVER,
        15, 100
    ),
    COMPLETE_DAILY_CHALLENGE_FOR_5_DAYS_IN_A_ROW(
        Category.SILVER,
        15, 100
    ),
    LEVEL_15TH(
        Category.SILVER,
        15, 100
    ),
    FIRST_POWER_UP(
        Category.SILVER,
        15, 100
    ),
    HAVE_1K_COINS(
        Category.SILVER,
        15, 100
    ),
    INVITE_FRIEND(
        Category.SILVER,
        15, 100
    ),
    CHANGE_PET(
        Category.SILVER,
        15, 100
    ),
    PET_DIED(
        Category.SILVER,
        15, 100
    ),
    FIRST_FOLLOW(
        Category.SILVER,
        15, 100
    ),
    FIRST_FOLLOWER(
        Category.SILVER,
        15, 100
    ),
    LEVEL_20TH(
        Category.GOLD,
        100, 300
    ),
    GAIN_999_XP_IN_A_DAY(
        Category.GOLD,
        100, 300
    ),
    COMPLETE_QUEST_FOR_100_DAYS_IN_A_ROW(
        Category.GOLD,
        100, 300
    ),
    FEEDBACK_SENT(
        Category.GOLD,
        100, 300
    );

    enum class Category {
        BRONZE, SILVER, GOLD;
    }
}