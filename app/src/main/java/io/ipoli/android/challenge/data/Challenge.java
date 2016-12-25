package io.ipoli.android.challenge.data;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.PeriodHistory;
import io.ipoli.android.quest.generators.RewardProvider;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class Challenge extends PersistedObject implements RewardProvider {

    private String name;

    private String category;

    private String reason1;
    private String reason2;
    private String reason3;

    private String expectedResult1;
    private String expectedResult2;
    private String expectedResult3;

    private Integer difficulty;

    private Long end;

    private Long completedAt;

    private Long coins;
    private Long experience;

    private Long totalMinutesSpent;
    private Long nextScheduledDate;

    // In chronological order
    private List<PeriodHistory> periodHistories;

    private Map<String, ChallengeQuest> challengeQuests;

    // true - quest is complete, false - not complete
    private Map<String, Boolean> questIds;
    private Map<String, Boolean> repeatingQuestIds;

    private String source;

    public Challenge() {
    }

    public Challenge(String name) {
        this.name = name;
        this.category = Category.PERSONAL.name();
        this.source = Constants.API_RESOURCE_SOURCE;
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
        setTotalMinutesSpent(0L);
    }

    public Long getTotalMinutesSpent() {
        return totalMinutesSpent;
    }

    public void setTotalMinutesSpent(Long totalMinutesSpent) {
        this.totalMinutesSpent = totalMinutesSpent;
    }

    public Long getNextScheduledDate() {
        return nextScheduledDate;
    }

    public void setNextScheduledDate(Long nextScheduledDate) {
        this.nextScheduledDate = nextScheduledDate;
    }

    public List<PeriodHistory> getPeriodHistories() {
        return periodHistories;
    }

    public void setPeriodHistories(List<PeriodHistory> periodHistories) {
        this.periodHistories = periodHistories;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReason1() {
        return reason1;
    }

    public void setReason1(String reason1) {
        if (StringUtils.isEmpty(reason1)) {
            reason1 = null;
        }
        this.reason1 = reason1;
    }

    public String getReason2() {
        return reason2;
    }

    public void setReason2(String reason2) {
        if (StringUtils.isEmpty(reason2)) {
            reason2 = null;
        }
        this.reason2 = reason2;
    }

    public String getReason3() {
        return reason3;
    }

    public void setReason3(String reason3) {
        if (StringUtils.isEmpty(reason3)) {
            reason3 = null;
        }
        this.reason3 = reason3;
    }

    public String getExpectedResult1() {
        return expectedResult1;
    }

    public void setExpectedResult1(String expectedResult1) {
        if (StringUtils.isEmpty(expectedResult1)) {
            expectedResult1 = null;
        }
        this.expectedResult1 = expectedResult1;
    }

    public String getExpectedResult2() {
        return expectedResult2;
    }

    public void setExpectedResult2(String expectedResult2) {
        if (StringUtils.isEmpty(expectedResult2)) {
            expectedResult2 = null;
        }
        this.expectedResult2 = expectedResult2;
    }

    public String getExpectedResult3() {
        return expectedResult3;
    }

    public void setExpectedResult3(String expectedResult3) {
        if (StringUtils.isEmpty(expectedResult3)) {
            expectedResult3 = null;
        }
        this.expectedResult3 = expectedResult3;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    @Exclude
    public void setDifficultyType(Difficulty difficulty) {
        this.difficulty = difficulty.getValue();
    }

    @Exclude
    public Date getEndDate() {
        return end != null ? new Date(end) : null;
    }

    @Exclude
    public void setEndDate(Date endDate) {
        end = endDate != null ? endDate.getTime() : null;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    @Exclude
    public Date getCompletedAtDate() {
        return completedAt != null ? new Date(completedAt) : null;
    }

    @Exclude
    public void setCompletedAtDate(Date completedAtDate) {
        completedAt = completedAtDate != null ? completedAtDate.getTime() : null;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    public Long getCoins() {
        return coins;
    }

    public void setCoins(Long coins) {
        this.coins = coins;
    }

    public Long getExperience() {
        return experience;
    }

    public void setExperience(Long experience) {
        this.experience = experience;
    }

    public String getCategory() {
        return category;
    }

    @Exclude
    public void setCategoryType(Category category) {
        this.category = category.name();
    }

    @Exclude
    public Category getCategoryType() {
        return Category.valueOf(category);
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public static Category getCategory(Challenge challenge) {
        return Category.valueOf(challenge.getCategory());
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Exclude
    public void addChallengeQuest(ChallengeQuest challengeQuest) {
        getChallengeQuests().put(challengeQuest.getQuestId(), challengeQuest);
    }

    public Map<String, ChallengeQuest> getChallengeQuests() {
        if (challengeQuests == null) {
            challengeQuests = new HashMap<>();
        }
        return challengeQuests;
    }

    public void setChallengeQuests(Map<String, ChallengeQuest> challengeQuests) {
        this.challengeQuests = challengeQuests;
    }

    public Map<String, Boolean> getQuestIds() {
        if (questIds == null) {
            questIds = new HashMap<>();
        }
        return questIds;
    }

    public void setQuestIds(Map<String, Boolean> questIds) {
        this.questIds = questIds;
    }

    public Map<String, Boolean> getRepeatingQuestIds() {
        if (repeatingQuestIds == null) {
            repeatingQuestIds = new HashMap<>();
        }
        return repeatingQuestIds;
    }

    public void setRepeatingQuestIds(Map<String, Boolean> repeatingQuestIds) {
        this.repeatingQuestIds = repeatingQuestIds;
    }

    @Exclude
    public void addQuestId(String id, boolean isComplete) {
        getQuestIds().put(id, isComplete);
    }

    @Exclude
    public void addRepeatingQuestId(String id, boolean isComplete) {
        getRepeatingQuestIds().put(id, isComplete);
    }
}
