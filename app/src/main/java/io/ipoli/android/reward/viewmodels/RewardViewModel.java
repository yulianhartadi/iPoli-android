package io.ipoli.android.reward.viewmodels;

import io.ipoli.android.reward.data.Reward;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/31/16.
 */
public class RewardViewModel {

    private Reward reward;

    private boolean canBeBought;

    public RewardViewModel(Reward reward, boolean canBeBought) {
        this.reward = reward;
        this.canBeBought = canBeBought;
    }

    public Reward getReward() {
        return reward;
    }

    public boolean canBeBought() {
        return canBeBought;
    }
}
