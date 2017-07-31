package io.ipoli.android.achievement.actions;

import io.ipoli.android.challenge.data.Challenge;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/26/17.
 */
public class CompleteChallengeAction extends SimpleAchievementAction {

    public Challenge challenge;

    public CompleteChallengeAction() {
        super(Action.COMPLETE_CHALLENGE);
    }

    public CompleteChallengeAction(Challenge challenge) {
        this();
        this.challenge = challenge;
    }
}
