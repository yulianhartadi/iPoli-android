package io.ipoli.android.player;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import io.ipoli.android.Constants;
import io.ipoli.android.player.events.PlayerXPIncreasedEvent;
import io.ipoli.android.player.events.PlayerLevelUpEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.events.CompleteQuestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/25/16.
 */
public class SimplePlayerService implements PlayerService {

    private final Bus eventBus;
    private final PlayerPersistenceService playerPersistenceService;

    public SimplePlayerService(Bus eventBus, PlayerPersistenceService playerPersistenceService) {
        this.eventBus = eventBus;
        this.playerPersistenceService = playerPersistenceService;
    }

    @Subscribe
    public void onQuestComplete(CompleteQuestEvent e) {
        Player player = playerPersistenceService.find();
        int currentXP = player.getExperience();
        int newXP = currentXP + Constants.COMPLETE_QUEST_DEFAULT_EXPERIENCE;
        int maxXPForCurrentLevel = LevelExperienceGenerator.experienceForLevel(player.getLevel());
        if (newXP >= maxXPForCurrentLevel) {
            int newLevel = player.getLevel() + 1;
            player.setLevel(newLevel);
            // Every level starts from 0 experience (or the leftover from the previous one)
            newXP = newXP - maxXPForCurrentLevel;
            updatePlayerXP(player, newXP);
            int maxXPForNewLevel = LevelExperienceGenerator.experienceForLevel(player.getLevel());
            eventBus.post(new PlayerLevelUpEvent(newLevel, newXP, maxXPForNewLevel));
        } else {
            updatePlayerXP(player, newXP);
            eventBus.post(new PlayerXPIncreasedEvent(currentXP, newXP));
        }
    }

    private void updatePlayerXP(Player player, int newXP) {
        player.setExperience(newXP);
        playerPersistenceService.save(player);
    }
}
