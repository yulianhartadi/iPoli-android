package io.ipoli.android.quest.generators;

import java.util.Random;

import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/1/16.
 */
public class CoinsRewardGenerator extends BaseRewardGenerator {

    public CoinsRewardGenerator(PlayerPersistenceService playerPersistenceService) {
        super(playerPersistenceService);
    }

    @Override
    public long generate(Challenge challenge) {
        long reward = generateForDailyChallenge() * 2;
        reward *= challenge.getDifficulty();
        return (long) (reward * getCoinsBonusMultiplier());
    }

    @Override
    public long generate(Quest quest) {
        long[] rewards = new long[]{2L, 5L, 7L, 10L};
        long reward = rewards[new Random().nextInt(rewards.length)];
        if (quest.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY) {
            reward *= 2;
        }
        return (long) (reward * getCoinsBonusMultiplier());
    }

    @Override
    public long generateForDailyChallenge() {
        long[] rewards = new long[]{20L, 30L, 40L, 50L, 80L};
        return (long) (rewards[new Random().nextInt(rewards.length)] * getCoinsBonusMultiplier());
    }
}
