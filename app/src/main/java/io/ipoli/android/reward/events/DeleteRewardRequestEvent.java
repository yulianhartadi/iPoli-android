package io.ipoli.android.reward.events;

import io.ipoli.android.reward.data.Reward;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/30/16.
 */
public class DeleteRewardRequestEvent {
    public final Reward reward;

    public DeleteRewardRequestEvent(Reward reward) {
        this.reward = reward;
    }
}
