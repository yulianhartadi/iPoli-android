package io.ipoli.android.quest.generators;

import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/1/16.
 */
public interface RewardGenerator {
    long generate(Challenge challenge);

    long generate(Quest quest);

    long generateForDailyChallenge();
}
