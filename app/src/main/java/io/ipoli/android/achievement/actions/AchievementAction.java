package io.ipoli.android.achievement.actions;

public interface AchievementAction {

    enum Action {
        COMPLETE_QUEST,
        SHARE_QUEST,
        COMPLETE_REPEATING_QUEST,
        CREATE_CHALLENGE,
        COMPLETE_DAILY_CHALLENGE,
        COMPLETE_CHALLENGE,
        USE_REWARD,
        CHANGE_AVATAR,
        CHANGE_PET,
        LEVEL_UP,
        BUY_POWER_UP,
        WIN_COINS,
        WIN_XP,
        INVITE_FRIEND,
        PET_DIED,
        FOLLOW,
        IS_FOLLOWED,
        SEND_FEEDBACK
    }

    Action getAction();
}