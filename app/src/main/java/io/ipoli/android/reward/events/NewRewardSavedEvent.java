package io.ipoli.android.reward.events;

import io.ipoli.android.reward.data.Reward;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/2/16.
 */
public class NewRewardSavedEvent {
    public final Reward reward;

    public NewRewardSavedEvent(Reward reward) {
        this.reward = reward;
    }
}
