package io.ipoli.android.achievement;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.otto.Bus;

import java.math.BigInteger;
import java.util.List;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.achievement.actions.AchievementAction;
import io.ipoli.android.achievement.events.AchievementsUnlockedEvent;
import io.ipoli.android.achievement.persistence.AchievementProgressPersistenceService;
import io.ipoli.android.achievement.ui.AchievementData;
import io.ipoli.android.achievement.ui.AchievementUnlocked;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.feed.persistence.FeedPersistenceService;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.player.events.LevelUpEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public class AchievementUnlockJobService extends JobService {

    @Inject
    FeedPersistenceService feedPersistenceService;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    AchievementProgressPersistenceService achievementProgressPersistenceService;

    @Inject
    AchievementUnlocker achievementUnlocker;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    Bus eventBus;

    @Override
    public void onCreate() {
        App.getAppComponent(this).inject(this);
        super.onCreate();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Class<? extends AchievementAction> actionClass;
        AchievementAction action;
        try {
            actionClass = (Class<? extends AchievementAction>) Class.forName(jobParameters.getExtras().getString(Constants.KEY_ACHIEVEMENT_ACTION_CLASS));
            action = objectMapper.readValue(jobParameters.getExtras().getString(Constants.KEY_ACHIEVEMENT_ACTION), actionClass);
        } catch (Exception e) {
            eventBus.post(new AppErrorEvent(e));
            jobFinished(jobParameters, false);
            return false;
        }

        createAsyncTask(jobParameters, action).execute();
        return true;
    }

    @NonNull
    private AsyncTask<Void, Void, List<Achievement>> createAsyncTask(final JobParameters jobParameters, final AchievementAction action) {
        return new AsyncTask<Void, Void, List<Achievement>>() {
            @Override
            protected List<Achievement> doInBackground(Void... voids) {
                Player player = playerPersistenceService.get();
                AchievementsProgress progress = achievementProgressPersistenceService.get();
                AchievementsProgressCoordinator.update(action, progress);

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

                new Handler(Looper.getMainLooper()).post(() -> playerPersistenceService.save(player));

                if(!achievementsToUnlock.isEmpty()) {
                    feedPersistenceService.incrementAchievements(achievementsToUnlock);
                }
                return achievementsToUnlock;
            }

            @Override
            protected void onPostExecute(List<Achievement> achievements) {
                if (achievements.isEmpty()) {
                    jobFinished(jobParameters, false);
                    return;
                }
                AchievementUnlocked achievementUnlocked = new AchievementUnlocked(getApplicationContext());
                achievementUnlocked.setRounded(true).setLarge(true).setTopAligned(true).setDismissible(true);
                AchievementData[] achievementsData = new AchievementData[achievements.size()];
                for (int i = 0; i < achievements.size(); i++) {
                    Achievement achievement = achievements.get(i);
                    AchievementData data = new AchievementData();
                    data.setTitle(getString(R.string.achievement_unlocked));
                    String subtitle = getString(achievement.name);
                    if (achievement.coins > 0 && achievement.experience > 0) {
                        subtitle = getString(R.string.achievement_with_reward, subtitle, achievement.experience, achievement.coins);
                    }
                    data.setSubtitle(subtitle);
                    data.setTextColor(Color.WHITE);
                    data.setBackgroundColor(Color.BLACK);
                    data.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_context_fun_white));
                    data.setIconBackgroundColor(Color.GREEN);
                    achievementsData[i] = data;
                }
                achievementUnlocked.show(achievementsData);
                eventBus.post(new AchievementsUnlockedEvent(achievements));
                jobFinished(jobParameters, false);
            }
        };
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
