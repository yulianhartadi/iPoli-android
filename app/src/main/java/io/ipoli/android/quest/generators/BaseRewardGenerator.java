package io.ipoli.android.quest.generators;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.LocalStorage;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/24/16.
 */
public abstract class BaseRewardGenerator implements RewardGenerator {

    private final LocalStorage localStorage;

    public BaseRewardGenerator(LocalStorage localStorage) {
        this.localStorage = localStorage;
    }

    protected double getXpBonusMultiplier() {
        int xpBonusPercentage = localStorage.readInt(Constants.XP_BONUS_PERCENTAGE, 0);
        return (xpBonusPercentage + 100) / 100.0;
    }

    protected double getCoinsBonusMultiplier() {
        int coinsBonusPercentage = localStorage.readInt(Constants.COINS_BONUS_PERCENTAGE, 0);
        return (coinsBonusPercentage + 100) / 100.0;
    }
}
