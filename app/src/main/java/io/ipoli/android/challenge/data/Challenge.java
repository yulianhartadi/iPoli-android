package io.ipoli.android.challenge.data;

import java.util.Date;

import io.ipoli.android.Constants;
import io.ipoli.android.app.net.RemoteObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IDGenerator;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.generators.RewardProvider;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class Challenge extends RealmObject implements RemoteObject<Challenge>, RewardProvider {

    @Required
    @PrimaryKey
    private String id;

    @Required
    private String name;

    private String category;

    private String reason1;
    private String reason2;
    private String reason3;

    private String expectedResult1;
    private String expectedResult2;
    private String expectedResult3;

    private Integer difficulty;

    private Date endDate;

    private Date completedAt;

    private Long coins;
    private Long experience;

    private String source;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    private Boolean needsSyncWithRemote;
    private String remoteId;
    private Boolean isDeleted;

    public Challenge() {
    }

    public Challenge(String name) {
        this.id = IDGenerator.generate();
        this.name = name;
        this.category = Category.PERSONAL.name();
        this.source = Constants.API_RESOURCE_SOURCE;
        this.createdAt = DateUtils.nowUTC();
        this.updatedAt = DateUtils.nowUTC();
        this.needsSyncWithRemote = true;
        this.isDeleted = false;
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
    public void markUpdated() {
        setNeedsSync();
        setUpdatedAt(DateUtils.nowUTC());
    }

    @Override
    public void setNeedsSync() {
        needsSyncWithRemote = true;
    }

    @Override
    public boolean needsSyncWithRemote() {
        return needsSyncWithRemote;
    }

    @Override
    public void setSyncedWithRemote() {
        needsSyncWithRemote = false;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
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

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
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

    public Category getCategory() {
        return Category.valueOf(category);
    }

    public void setCategory(Category category) {
        this.category = category.name();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String getRemoteId() {
        return remoteId;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public void markDeleted() {
        isDeleted = true;
        markUpdated();
    }

    @Override
    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }
}
