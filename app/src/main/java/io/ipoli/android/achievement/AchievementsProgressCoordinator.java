package io.ipoli.android.achievement;

import io.ipoli.android.achievement.actions.AchievementAction;
import io.ipoli.android.achievement.actions.CompleteDailyChallengeAction;
import io.ipoli.android.achievement.actions.CompleteQuestAction;
import io.ipoli.android.achievement.actions.LevelUpAction;
import io.ipoli.android.achievement.actions.WinCoinsAction;
import io.ipoli.android.challenge.data.Challenge;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/24/17.
 */
public class AchievementsProgressCoordinator {

    public static void update(AchievementAction action, AchievementsProgress progress) {
        switch (action.getAction()) {
            case COMPLETE_QUEST:
                CompleteQuestAction completeQuestAction = (CompleteQuestAction) action;
                progress.incrementCompletedQuestCount();
                progress.incrementCompletedQuestsInADay();
                progress.incrementExperienceInADay(completeQuestAction.quest.getExperience().intValue());
                progress.incrementCompletedQuestsInARow();
                break;
            case LEVEL_UP:
                int newPlayerLevel = ((LevelUpAction) action).level;
                progress.setPlayerLevel(newPlayerLevel);
                break;
            case COMPLETE_DAILY_CHALLENGE:
                CompleteDailyChallengeAction completeDailyChallengeAction = (CompleteDailyChallengeAction) action;
                progress.incrementCompletedDailyChallengesInARow();
                progress.incrementCompletedDailyChallengeCount();
                Challenge challenge = completeDailyChallengeAction.challenge;
                progress.incrementExperienceInADay(challenge.getExperience().intValue());
                break;
            case COMPLETE_REPEATING_QUEST:
                progress.incrementRepeatingQuestCreatedCount();
                break;
            case CREATE_CHALLENGE:
                progress.incrementChallengeAcceptedCount();
                break;
            case USE_REWARD:
                progress.incrementRewardUsedCount();
                break;
            case ADD_POST:
                progress.incrementPostAddedCount();
                break;
            case CHANGE_AVATAR:
                progress.incrementAvatarChangedCount();
                break;
            case SEND_FEEDBACK:
                progress.incrementFeedbackSent();
                break;
            case BUY_POWER_UP:
                progress.incrementPowerUpBoughtCount();
                break;
            case WIN_COINS:
                WinCoinsAction winCoinsAction = (WinCoinsAction) action;
                progress.setLifeCoinCount(winCoinsAction.coins);
                break;
            case INVITE_FRIEND:
                progress.incrementInvitedFriendCount();
                break;
            case CHANGE_PET:
                progress.incrementPetChangedCount();
                break;
        }
    }
}
