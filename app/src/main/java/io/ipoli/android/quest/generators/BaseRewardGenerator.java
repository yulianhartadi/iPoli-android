package io.ipoli.android.quest.generators;

import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/24/16.
 */
public abstract class BaseRewardGenerator implements RewardGenerator {

    private final PlayerPersistenceService playerPersistenceService;

    public BaseRewardGenerator(PlayerPersistenceService playerPersistenceService) {
        this.playerPersistenceService = playerPersistenceService;
    }

    protected double getXpBonusMultiplier() {
        int xpBonusPercentage = playerPersistenceService.get().getPet().getExperienceBonusPercentage();
        return (xpBonusPercentage + 100) / 100.0;
    }

    protected double getCoinsBonusMultiplier() {
        int coinsBonusPercentage = playerPersistenceService.get().getPet().getCoinsBonusPercentage();
        return (coinsBonusPercentage + 100) / 100.0;
    }
}
