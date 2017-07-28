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
    private Integer createdChallengeCount;
    private Integer avatarChangedCount;
    private Integer completedDailyChallengeCount;
    private Integer postAddedCount;
    private Integer createdRepeatedQuestCount;
    private Integer rewardUsedCount;
    private Long lifeCoinCount;
    private Integer invitedFriendCount;
    private Integer petChangeCount;
    private Integer petDiedCount;
    private Integer followCount;
    private Integer followerCount;
    private Integer feedbackSentCount;
    private Integer powerUpCount;
    private Integer completedChallengesCount;

    public AchievementsProgress() {
        super(TYPE);
    }

    @JsonIgnore
    public void incrementCompletedQuestCount() {
        setCompletedQuestCount(getCompletedQuestCount() + 1);
    }

    @JsonIgnore
    public void incrementCompletedDailyChallengeCount() {
        setCompletedDailyChallengeCount(getCompletedDailyChallengeCount() + 1);
    }

    @JsonIgnore
    public void incrementChallengeAcceptedCount() {
        setCreatedChallengeCount(getCreatedChallengeCount() + 1);
    }

    @JsonIgnore
    public void incrementCompletedChallengesCount() {
        setCompletedChallengesCount(getCompletedChallengesCount() + 1);
    }

    @JsonIgnore
    public void incrementAvatarChangedCount() {
        setAvatarChangedCount(getAvatarChangedCount() + 1);
    }

    @JsonIgnore
    public void incrementPostAddedCount() {
        setPostAddedCount(getPostAddedCount() + 1);
    }

    @JsonIgnore
    public void incrementRepeatingQuestCreatedCount() {
        setCreatedRepeatedQuestCount(getCreatedRepeatedQuestCount() + 1);
    }

    @JsonIgnore
    public void incrementRewardUsedCount() {
        setRewardUsedCount(getRewardUsedCount() + 1);
    }

    @JsonIgnore
    public void incrementCompletedQuestsInADay() {
        ActionCountPerDay completedQuestsInADay = getCompletedQuestsInADay();
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
        ActionCountPerDay experienceInADay = getExperienceInADay();
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
        ActionCountPerDay completedQuestsInARow = getCompletedQuestsInARow();
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
        ActionCountPerDay completedDailyChallengesInARow = getCompletedDailyChallengesInARow();
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

    @JsonIgnore
    public void incrementFeedbackSent() {
        setFeedbackSentCount(getFeedbackSentCount() + 1);
    }

    @JsonIgnore
    public void incrementPowerUpBoughtCount() {
        setPowerUpCount(getPowerUpCount() + 1);
    }

    @JsonIgnore
    public void incrementInvitedFriendCount() {
        setInvitedFriendCount(getInvitedFriendCount() + 1);
    }

    @JsonIgnore
    public void incrementPetChangedCount() {
        setPetChangeCount(getPetChangeCount() + 1);
    }

    @JsonIgnore
    public void incrementPetDiedCount() {
        setPetDiedCount(getPetDiedCount() + 1);
    }

    @JsonIgnore
    public void incrementFollowCount() {
        setFollowCount(getFollowerCount() + 1);
    }

    @JsonIgnore
    public void incrementFollowerCount() {
        setFollowerCount(getFollowerCount() + 1);
    }

    @JsonIgnore
    private ActionCountPerDay getDefaultActionCountPerDay() {
        return new ActionCountPerDay(0, LocalDate.now());
    }

    public Integer getCompletedQuestCount() {
        if (completedQuestCount == null) {
            completedQuestCount = 0;
        }
        return completedQuestCount;
    }

    public void setCompletedQuestCount(Integer completedQuestCount) {
        this.completedQuestCount = completedQuestCount;
    }

    public ActionCountPerDay getCompletedQuestsInADay() {
        if (completedQuestsInADay == null) {
            completedQuestsInADay = getDefaultActionCountPerDay();
        }
        return completedQuestsInADay;
    }


    public void setCompletedQuestsInADay(ActionCountPerDay completedQuestsInADay) {
        this.completedQuestsInADay = completedQuestsInADay;
    }

    public ActionCountPerDay getExperienceInADay() {
        if (experienceInADay == null) {
            experienceInADay = getDefaultActionCountPerDay();
        }
        return experienceInADay;
    }

    public void setExperienceInADay(ActionCountPerDay experienceInADay) {
        this.experienceInADay = experienceInADay;
    }

    public ActionCountPerDay getCompletedQuestsInARow() {
        if (completedQuestsInARow == null) {
            completedQuestsInARow = getDefaultActionCountPerDay();
        }
        return completedQuestsInARow;
    }

    public void setCompletedQuestsInARow(ActionCountPerDay completedQuestsInARow) {
        this.completedQuestsInARow = completedQuestsInARow;
    }

    public ActionCountPerDay getCompletedDailyChallengesInARow() {
        if (completedDailyChallengesInARow == null) {
            completedDailyChallengesInARow = getDefaultActionCountPerDay();
        }
        return completedDailyChallengesInARow;
    }

    public void setCompletedDailyChallengesInARow(ActionCountPerDay completedDailyChallengesInARow) {
        this.completedDailyChallengesInARow = completedDailyChallengesInARow;
    }

    public Integer getPlayerLevel() {
        if (playerLevel == null) {
            playerLevel = Constants.DEFAULT_PLAYER_LEVEL;
        }
        return playerLevel;
    }

    public void setPlayerLevel(Integer playerLevel) {
        this.playerLevel = playerLevel;
    }

    public Integer getCreatedChallengeCount() {
        if (createdChallengeCount == null) {
            createdChallengeCount = 0;
        }
        return createdChallengeCount;
    }

    public void setCreatedChallengeCount(Integer createdChallengeCount) {
        this.createdChallengeCount = createdChallengeCount;
    }

    public Integer getAvatarChangedCount() {
        if (avatarChangedCount == null) {
            avatarChangedCount = 0;
        }
        return avatarChangedCount;
    }

    public void setAvatarChangedCount(Integer avatarChangedCount) {
        this.avatarChangedCount = avatarChangedCount;
    }

    public Integer getCompletedDailyChallengeCount() {
        if (completedDailyChallengeCount == null) {
            completedDailyChallengeCount = 0;
        }
        return completedDailyChallengeCount;
    }

    public void setCompletedDailyChallengeCount(Integer completedDailyChallengeCount) {
        this.completedDailyChallengeCount = completedDailyChallengeCount;
    }

    public Integer getPostAddedCount() {
        if (postAddedCount == null) {
            postAddedCount = 0;
        }
        return postAddedCount;
    }

    public void setPostAddedCount(Integer postAddedCount) {
        this.postAddedCount = postAddedCount;
    }

    public Integer getCreatedRepeatedQuestCount() {
        if (createdRepeatedQuestCount == null) {
            createdRepeatedQuestCount = 0;
        }
        return createdRepeatedQuestCount;
    }

    public void setCreatedRepeatedQuestCount(Integer createdRepeatedQuestCount) {
        this.createdRepeatedQuestCount = createdRepeatedQuestCount;
    }

    public Integer getRewardUsedCount() {
        if (rewardUsedCount == null) {
            rewardUsedCount = 0;
        }
        return rewardUsedCount;
    }

    public void setRewardUsedCount(Integer rewardUsedCount) {
        this.rewardUsedCount = rewardUsedCount;
    }

    public void setLifeCoinCount(Long lifeCoinCount) {
        this.lifeCoinCount = lifeCoinCount;
    }

    public long getLifeCoinCount() {
        if (lifeCoinCount == null) {
            lifeCoinCount = Constants.DEFAULT_PLAYER_COINS;
        }
        return lifeCoinCount;
    }

    public int getInvitedFriendCount() {
        if (invitedFriendCount == null) {
            invitedFriendCount = 0;
        }
        return invitedFriendCount;
    }

    public void setInvitedFriendCount(Integer invitedFriendCount) {
        this.invitedFriendCount = invitedFriendCount;
    }

    public int getPetChangeCount() {
        if (petChangeCount == null) {
            petChangeCount = 0;
        }
        return petChangeCount;
    }

    public void setPetChangeCount(Integer petChangeCount) {
        this.petChangeCount = petChangeCount;
    }

    public int getPetDiedCount() {
        if (petDiedCount == null) {
            petDiedCount = 0;
        }
        return petDiedCount;
    }

    public void setPetDiedCount(Integer petDiedCount) {
        this.petDiedCount = petDiedCount;
    }

    public int getFollowCount() {
        if (followCount == null) {
            followCount = 0;
        }
        return followCount;
    }

    public void setFollowCount(Integer followCount) {
        this.followCount = followCount;
    }

    public int getFollowerCount() {
        if (followerCount == null) {
            followerCount = 0;
        }
        return followerCount;
    }

    public void setFollowerCount(Integer followerCount) {
        this.followerCount = followerCount;
    }

    public Integer getFeedbackSentCount() {
        if (feedbackSentCount == null) {
            feedbackSentCount = 0;
        }
        return feedbackSentCount;
    }

    public void setFeedbackSentCount(Integer feedbackSentCount) {
        this.feedbackSentCount = feedbackSentCount;
    }

    public Integer getPowerUpCount() {
        if (powerUpCount == null) {
            powerUpCount = 0;
        }
        return powerUpCount;
    }

    public void setPowerUpCount(Integer powerUpCount) {
        this.powerUpCount = powerUpCount;
    }

    public Integer getCompletedChallengesCount() {
        if(completedChallengesCount == null) {
            completedChallengesCount = 0;
        }
        return completedChallengesCount;
    }

    public void setCompletedChallengesCount(Integer completedChallengesCount) {
        this.completedChallengesCount = completedChallengesCount;
    }
}