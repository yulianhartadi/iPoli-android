package io.ipoli.android.quest.data;

import io.ipoli.android.app.utils.StringUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/24/16.
 */
public class DayQuest {
    private String questId;
    private String name;
    private String category;
    private int startMinute;
    private int duration;
    private boolean isFromRepeatingQuest;
    private boolean isForChallenge;
    private Long completedAt;
    private int priority;

    public DayQuest() {
        
    }
    
    public DayQuest(Quest quest) {
        setQuestId(quest.getId());
        setName(quest.getName());
        setCategory(quest.getCategory());
        setStartMinute(quest.getStartMinute());
        setDuration(quest.getDuration());
        setIsFromRepeatingQuest(quest.isRepeatingQuest());
        setIsForChallenge(!StringUtils.isEmpty(quest.getChallengeId()));
        setCompletedAt(quest.getCompletedAt());
        setPriority(quest.getPriority());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setIsFromRepeatingQuest(boolean isFromRepeatingQuest) {
        this.isFromRepeatingQuest = isFromRepeatingQuest;
    }

    public void setIsForChallenge(boolean isForChallenge) {
        this.isForChallenge = isForChallenge;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isFromRepeatingQuest() {
        return isFromRepeatingQuest;
    }

    public boolean isForChallenge() {
        return isForChallenge;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public int getPriority() {
        return priority;
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }
}
