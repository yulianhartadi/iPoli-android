package io.ipoli.android.achievement;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;

import com.squareup.otto.Bus;

import java.math.BigInteger;
import java.util.List;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.achievement.persistence.AchievementProgressPersistenceService;
import io.ipoli.android.achievement.ui.AchievementData;
import io.ipoli.android.achievement.ui.AchievementUnlocked;
import io.ipoli.android.app.App;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.player.events.LevelUpEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public class AchievementUnlockJobService extends JobService {

    public static final int JOB_ID = 3;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    AchievementProgressPersistenceService achievementProgressPersistenceService;

    @Inject
    AchievementUnlocker achievementUnlocker;

    @Inject
    Bus eventBus;

    @Override
    public void onCreate() {
        App.getAppComponent(this).inject(this);
        super.onCreate();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        AchievementAction action = AchievementAction.valueOf(jobParameters.getExtras().getString(Constants.KEY_ACHIEVEMENT_ACTION));
        new AsyncTask<Void, Void, List<Achievement>>() {
            @Override
            protected List<Achievement> doInBackground(Void... voids) {
                Player player = playerPersistenceService.get();
                AchievementsProgress progress = achievementProgressPersistenceService.get();
                AchievementsProgressUpdater.update(action, progress);

                List<Achievement> achievementsToUnlock = achievementUnlocker.findUnlocked(
                        player.getAchievements().keySet(),
                        progress);

                achievementProgressPersistenceService.save(progress);

                player.unlockAchievements(achievementsToUnlock);
                for (Achievement achievement : achievementsToUnlock) {
                    player.addExperience(achievement.experience);
                    player.addCoins(achievement.coins);
                }
                increasePlayerLevelIfNeeded(player);
                playerPersistenceService.save(player);

                return achievementsToUnlock;
            }

            @Override
            protected void onPostExecute(List<Achievement> achievements) {
                AchievementUnlocked achievementUnlocked = new AchievementUnlocked(getApplicationContext());
                achievementUnlocked.setRounded(true).setLarge(true).setTopAligned(true).setDismissible(true);
                AchievementData[] achievementsData = new AchievementData[achievements.size()];
                for (int i = 0; i < achievements.size(); i++) {
                    Achievement achievement = achievements.get(i);
                    AchievementData data = new AchievementData();
                    data.setTitle("Achievement Unlocked");
                    data.setSubtitle(getString(achievement.description));
                    data.setTextColor(Color.WHITE);
                    data.setBackgroundColor(Color.BLACK);
                    data.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.subscriptions_badge));
                    data.setIconBackgroundColor(Color.GREEN);
                    achievementsData[i] = data;
                }
                achievementUnlocked.show(achievementsData);
            }
        }.execute();

        return true;
    }

    private void increasePlayerLevelIfNeeded(Player player) {
        if (shouldIncreaseLevel(player)) {
            player.setLevel(player.getLevel() + 1);
            while (shouldIncreaseLevel(player)) {
                player.setLevel(player.getLevel() + 1);
            }
            eventBus.post(new LevelUpEvent(player.getLevel()));
        }
    }

    private boolean shouldIncreaseLevel(Player player) {
        return new BigInteger(player.getExperience()).compareTo(ExperienceForLevelGenerator.forLevel(player.getLevel() + 1)) >= 0;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
