package io.ipoli.android.quest.generators;

import java.util.Random;

import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/1/16.
 */
public class ExperienceRewardGenerator extends BaseRewardGenerator {

    public ExperienceRewardGenerator(PlayerPersistenceService playerPersistenceService) {
        super(playerPersistenceService);
    }

    @Override
    public long generate(Challenge challenge) {
        long[] rewards = new long[]{20L, 30L, 50L, 80L, 100L};
        long reward = rewards[new Random().nextInt(rewards.length)] * 2;
        reward *= challenge.getDifficulty();
        return (long) (reward * getXpBonusMultiplier());
    }

    @Override
    public long generate(Quest quest) {
        long[] rewards = new long[]{5L, 10L, 15L, 20L, 30L};
        long reward = rewards[new Random().nextInt(rewards.length)];
        if (quest.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY) {
            reward *= 2;
        }
        return (long) (reward * getXpBonusMultiplier());
    }

    @Override
    public long generateForDailyChallenge() {
        long[] rewards = new long[]{20L, 30L, 50L, 80L, 100L};
        return (long) (rewards[new Random().nextInt(rewards.length)] * getXpBonusMultiplier());
    }
}