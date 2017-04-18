package io.ipoli.android.challenge.data;

import android.support.v4.util.Pair;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
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
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.QuestData;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.generators.RewardProvider;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class Challenge extends PersistedObject implements RewardProvider {

    public static final String TYPE = "challenge";

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

    @JsonIgnore
    private Map<String, Quest> challengeQuests;

    @JsonIgnore
    private Map<String, RepeatingQuest> challengeRepeatingQuests;

    @JsonIgnore
    private Map<String, QuestData> questsData;

    private String source;

    public Challenge() {
        super(TYPE);
    }

    public Challenge(String name) {
        super(TYPE);
        this.name = name;
        this.category = Category.PERSONAL.name();
        this.source = Constants.API_RESOURCE_SOURCE;
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
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


    public void setDifficultyType(Difficulty difficulty) {
        this.difficulty = difficulty.getValue();
    }

    @JsonIgnore
    public LocalDate getEndDate() {
        return end != null ? DateUtils.fromMillis(end) : null;
    }

    @JsonIgnore
    public void setEndDate(LocalDate endDate) {
        end = endDate != null ? DateUtils.toMillis(endDate) : null;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    @JsonIgnore
    public Date getCompletedAtDate() {
        return completedAt != null ? new Date(completedAt) : null;
    }

    @JsonIgnore
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

    @JsonIgnore
    public void setCategoryType(Category category) {
        this.category = category.name();
    }

    @JsonIgnore
    public Category getCategoryType() {
        return Category.valueOf(category);
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @JsonIgnore
    public static Category getCategory(Challenge challenge) {
        return Category.valueOf(challenge.getCategory());
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @JsonIgnore
    public Map<String, Quest> getChallengeQuests() {
        if (challengeQuests == null) {
            challengeQuests = new HashMap<>();
        }
        return challengeQuests;
    }

    @JsonIgnore
    public void setChallengeQuests(Map<String, Quest> challengeQuests) {
        this.challengeQuests = challengeQuests;
    }

    @JsonIgnore
    public void addChallengeQuest(Quest quest) {
        getChallengeQuests().put(quest.getId(), quest);
    }

    @JsonIgnore
    public Map<String, RepeatingQuest> getChallengeRepeatingQuests() {
        if (challengeRepeatingQuests == null) {
            challengeRepeatingQuests = new HashMap<>();
        }
        return challengeRepeatingQuests;
    }

    @JsonIgnore
    public void addChallengeRepeatingQuest(RepeatingQuest repeatingQuest) {
        getChallengeRepeatingQuests().put(repeatingQuest.getId(), repeatingQuest);
    }

    @JsonIgnore
    public void setChallengeRepeatingQuests(Map<String, RepeatingQuest> challengeRepeatingQuests) {
        this.challengeRepeatingQuests = challengeRepeatingQuests;
    }

    @JsonIgnore
    public Map<String, QuestData> getQuestsData() {
        if (questsData == null) {
            questsData = new HashMap<>();
        }
        return questsData;
    }

    @JsonIgnore
    public void addQuestData(String id, QuestData questData) {
        getQuestsData().put(id, questData);
    }

    @JsonIgnore
    public LocalDate getNextScheduledDate(LocalDate currentDate) {
        LocalDate nextDate = null;
        for (QuestData qd : questsData.values()) {
            if (!qd.isComplete() && qd.getScheduledDate() != null && !currentDate.isAfter(qd.getScheduledDate())) {
                if (nextDate == null || nextDate.isAfter(qd.getScheduledDate())) {
                    nextDate = qd.getScheduledDate();
                }
            }
        }
        return nextDate;
    }

    @JsonIgnore
    public int getTotalTimeSpent() {
        int timeSpent = 0;
        for (QuestData questData : getQuestsData().values()) {
            if (questData.isComplete()) {
                timeSpent += questData.getDuration();
            }
        }
        return timeSpent;
    }

    @JsonIgnore
    public List<PeriodHistory> getPeriodHistories(LocalDate currentDate) {
        List<PeriodHistory> result = new ArrayList<>();
        List<Pair<LocalDate, LocalDate>> pairs =
                DateUtils.getBoundsFor4WeeksInThePast(currentDate);

        for (Pair<LocalDate, LocalDate> p : pairs) {
            result.add(new PeriodHistory(p.first, p.second));
        }

        for (QuestData qd : getQuestsData().values()) {
            if (!qd.isComplete()) {
                continue;
            }
            for (PeriodHistory p : result) {
                if (DateUtils.isBetween(qd.getScheduledDate(), p.getStartDate(), p.getEndDate())) {
                    p.increaseCompletedCount();
                    break;
                }
            }
        }

        return result;
    }

    @JsonIgnore
    public int getTotalQuestCount() {
        return getQuestsData().size();
    }


    @JsonIgnore
    public int getCompletedQuestCount() {
        int count = 0;
        for (QuestData questData : getQuestsData().values()) {
            if (questData.isComplete()) {
                count++;
            }
        }
        return count;
    }
}
