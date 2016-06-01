package io.ipoli.android.reward.events;

import io.ipoli.android.reward.data.Reward;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/30/16.
 */
public class EditRewardRequestEvent {
    public final Reward reward;

    public EditRewardRequestEvent(Reward reward) {
        this.reward = reward;
    }
}
