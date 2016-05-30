package io.ipoli.android.reward.events;

import io.ipoli.android.reward.data.Reward;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/30/16.
 */
public class DeleteRewardRequestEvent {
    public final Reward reward;
    public final int position;

    public DeleteRewardRequestEvent(Reward reward, int position) {
        this.reward = reward;
        this.position = position;
    }
}
