package io.ipoli.android.quest.generators;

import java.util.Random;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/1/16.
 */
public class ExperienceRewardGenerator implements RewardGenerator {

    @Override
    public long generate(Quest quest) {
        Long[] rewards = new Long[] {5L, 10L, 15L, 20L, 30L};
        return rewards[new Random().nextInt(rewards.length)];
    }

    @Override
    public long generate() {
        return generate(null);
    }
}