package io.ipoli.android.achievement;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.threeten.bp.LocalDate;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public class AchievementsProgress extends PersistedObject {

    public static final String TYPE = "achievementProgress";

    private Integer completedQuestCount;
    private ActionCountPerDay completedQuestsInADay;
    private ActionCountPerDay experienceInADay;
    private ActionCountPerDay completedQuestsInARow;
    private ActionCountPerDay completedDailyChallengesInARow;
    private Integer playerLevel;
    private Integer challengeAcceptedCount;
    private Integer avatarChangedCount;
    private Integer completedDailyChallengeCount;
    private Integer postAddedCount;
    private Integer repeatedQuestAddedCount;

    public AchievementsProgress() {
        super(TYPE);
    }

    public static AchievementsProgress create() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setCompletedQuestsInADay(new ActionCountPerDay(0, LocalDate.now()));
        progress.setExperienceInADay(new ActionCountPerDay(0, LocalDate.now()));
        progress.setCompletedQuestsInARow(new ActionCountPerDay(0, LocalDate.now()));
        progress.setCompletedDailyChallengesInARow(new ActionCountPerDay(0, LocalDate.now()));
        progress.setCompletedQuestCount(0);
        progress.setAvatarChangedCount(0);
        progress.setChallengeAcceptedCount(0);
        progress.setCompletedDailyChallengeCount(0);
        progress.setPostAddedCount(0);
        progress.setRepeatedQuestAddedCount(0);
        progress.setPlayerLevel(Constants.DEFAULT_PLAYER_LEVEL);
        return progress;
    }

    @JsonIgnore
    public void incrementCompletedQuestCount() {
        completedQuestCount++;
    }

    @JsonIgnore
    public void incrementCompletedDailyChallengeCount() {
        completedDailyChallengeCount++;
    }

    @JsonIgnore
    public void incrementChallengeAcceptedCount() {
        challengeAcceptedCount++;
    }

    @JsonIgnore
    public void incrementAvatarChangedCount() {
        avatarChangedCount++;
    }

    @JsonIgnore
    public void incrementPostAddedCount() {
        postAddedCount++;
    }

    @JsonIgnore
    public void incrementRepeatingQuestAddedCount() {
        repeatedQuestAddedCount++;
    }

    @JsonIgnore
    public void incrementCompletedQuestsInADay() {
        long today = DateUtils.toMillis(LocalDate.now());
        if (completedQuestsInADay.getDate() == today) {
            completedQuestsInADay.increment();
        } else {
            completedQuestsInADay.setDate(today);
            completedQuestsInADay.setCount(1);
        }
    }

    @JsonIgnore
    public void incrementExperienceInADay(int experience) {
        long today = DateUtils.toMillis(LocalDate.now());
        if (experienceInADay.getDate() == today) {
            experienceInADay.increment(experience);
        } else {
            experienceInADay.setDate(today);
            experienceInADay.setCount(experience);
        }
    }

    @JsonIgnore
    public void incrementCompletedQuestsInARow() {
        long today = DateUtils.toMillis(LocalDate.now());
        long yesterday = DateUtils.toMillis(LocalDate.now().minusDays(1));
        if (completedQuestsInARow.getCount() == 0 || completedQuestsInARow.getDate() == yesterday) {
            completedQuestsInARow.increment();
            completedQuestsInARow.setDate(today);
        } else if (completedQuestsInARow.getDate() < yesterday) {
            completedQuestsInARow.setDate(today);
            completedQuestsInARow.setCount(1);
        }
    }

    @JsonIgnore
    public void incrementCompletedDailyChallengesInARow() {
        long today = DateUtils.toMillis(LocalDate.now());
        long yesterday = DateUtils.toMillis(LocalDate.now().minusDays(1));
        if (completedDailyChallengesInARow.getCount() == 0 || completedDailyChallengesInARow.getDate() == yesterday) {
            completedDailyChallengesInARow.increment();
            completedDailyChallengesInARow.setDate(today);
        } else if (completedDailyChallengesInARow.getDate() < yesterday) {
            completedDailyChallengesInARow.setDate(today);
            completedDailyChallengesInARow.setCount(1);
        }
    }

    public ActionCountPerDay getCompletedDailyChallengesInARow() {
        return completedDailyChallengesInARow;
    }

    public void setCompletedDailyChallengesInARow(ActionCountPerDay completedDailyChallengesInARow) {
        this.completedDailyChallengesInARow = completedDailyChallengesInARow;
    }

    public void setCompletedQuestCount(Integer completedQuestCount) {
        this.completedQuestCount = completedQuestCount;
    }

    public int getCompletedQuestCount() {
        return completedQuestCount;
    }

    public ActionCountPerDay getCompletedQuestsInADay() {
        return completedQuestsInADay;
    }

    public void setCompletedQuestsInADay(ActionCountPerDay completedQuestsInADay) {
        this.completedQuestsInADay = completedQuestsInADay;
    }

    public void setExperienceInADay(ActionCountPerDay experienceInADay) {
        this.experienceInADay = experienceInADay;
    }

    public ActionCountPerDay getExperienceInADay() {
        return experienceInADay;
    }

    public void setCompletedQuestsInARow(ActionCountPerDay completedQuestsInARow) {
        this.completedQuestsInARow = completedQuestsInARow;
    }

    public ActionCountPerDay getCompletedQuestsInARow() {
        return completedQuestsInARow;
    }

    public void setPlayerLevel(int playerLevel) {
        this.playerLevel = playerLevel;
    }

    public int getPlayerLevel() {
        return playerLevel;
    }

    public int getChallengeAcceptedCount() {
        return challengeAcceptedCount;
    }

    public int getAvatarChangedCount() {
        return avatarChangedCount;
    }

    public int getCompletedDailyChallengeCount() {
        return completedDailyChallengeCount;
    }

    public int getPostAddedCount() {
        return postAddedCount;
    }

    public int getRepeatedQuestAddedCount() {
        return repeatedQuestAddedCount;
    }

    public void setChallengeAcceptedCount(int challengeAcceptedCount) {
        this.challengeAcceptedCount = challengeAcceptedCount;
    }

    public void setAvatarChangedCount(int avatarChangedCount) {
        this.avatarChangedCount = avatarChangedCount;
    }

    public void setCompletedDailyChallengeCount(int completedDailyChallengeCount) {
        this.completedDailyChallengeCount = completedDailyChallengeCount;
    }

    public void setPostAddedCount(int postAddedCount) {
        this.postAddedCount = postAddedCount;
    }

    public void setRepeatedQuestAddedCount(int repeatedQuestAddedCount) {
        this.repeatedQuestAddedCount = repeatedQuestAddedCount;
    }
}