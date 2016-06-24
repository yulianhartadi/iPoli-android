package io.ipoli.android.quest.generators;

import java.util.Random;

import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/1/16.
 */
public class ExperienceRewardGenerator implements RewardGenerator {

    @Override
    public long generate(Challenge challenge) {
        long reward = generateForDailyChallenge() * 2;
        reward *= challenge.getDifficulty();
        return reward;
    }

    @Override
    public long generate(Quest quest) {
        long[] rewards = new long[]{5L, 10L, 15L, 20L, 30L};
        long reward = rewards[new Random().nextInt(rewards.length)];
        if (quest.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY) {
            reward *= 2;
        }
        return reward;
    }

    @Override
    public long generateForDailyChallenge() {
        long[] rewards = new long[]{20L, 30L, 50L, 80L, 100L};
        return rewards[new Random().nextInt(rewards.length)];
    }
}