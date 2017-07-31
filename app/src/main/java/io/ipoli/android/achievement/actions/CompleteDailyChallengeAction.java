package io.ipoli.android.achievement.actions;

import io.ipoli.android.challenge.data.Challenge;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/26/17.
 */
public class CompleteDailyChallengeAction extends SimpleAchievementAction {

    public Challenge challenge;

    public CompleteDailyChallengeAction() {
        super(Action.COMPLETE_DAILY_CHALLENGE);
    }

    public CompleteDailyChallengeAction(Challenge challenge) {
        this();
        this.challenge = challenge;
    }
}
