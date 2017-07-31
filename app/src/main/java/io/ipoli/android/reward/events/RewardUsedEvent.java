package io.ipoli.android.reward.events;

import io.ipoli.android.reward.data.Reward;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/25/17.
 */
public class RewardUsedEvent {

    public final Reward reward;

    public RewardUsedEvent(Reward reward) {
        this.reward = reward;
    }
}
