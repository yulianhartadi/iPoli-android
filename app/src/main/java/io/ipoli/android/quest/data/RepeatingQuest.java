package io.ipoli.android.quest.data;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/26/16.
 */
public class RepeatingQuest extends PersistedObject implements BaseQuest {

    private String rawText;

    private String name;

    private String category;

    private boolean allDay;

    private Integer priority;

    private Integer startMinute;

    private String preferredStartTime;
    private Boolean flexibleStartTime;

    private Integer duration;
    private List<Reminder> reminders;
    private List<SubQuest> subQuests;

    private Recurrence recurrence;

    private String note;

    private String challengeId;

    private String source;

    private SourceMapping sourceMapping;

    public RepeatingQuest() {
    }

    public void setDuration(Integer duration) {
        this.duration = (int) Math.min(TimeUnit.HOURS.toMinutes(Constants.MAX_QUEST_DURATION_HOURS), duration);
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getRawText() {
        return rawText;
    }

    public int getDuration() {
        return duration != null ? duration : 0;
    }

    public void setStartMinute(Integer startMinute) {
        this.startMinute = startMinute;
    }

    public static Time getStartTime(RepeatingQuest quest) {
        if (quest.getStartMinute() < 0) {
            return null;
        }
        return Time.of(quest.getStartMinute());
    }

    public RepeatingQuest(String rawText) {
        this.rawText = rawText;
        this.createdAt = DateUtils.nowUTC();
        this.updatedAt = DateUtils.nowUTC();
        this.category = Category.PERSONAL.name();
        this.flexibleStartTime = false;
        this.source = Constants.API_RESOURCE_SOURCE;
        this.isDeleted = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Category getCategory() {
        return Category.valueOf(category);
    }

    public void setCategory(Category category) {
        this.category = category.name();
    }

    public static void setStartTime(RepeatingQuest quest, Time time) {
        if (time != null) {
            quest.setStartMinute(time.toMinutesAfterMidnight());
        } else {
            quest.setStartMinute(null);
        }
    }

    public int getStartMinute() {
        return startMinute != null ? startMinute : -1;
    }

    public List<SubQuest> getSubQuests() {
        return subQuests;
    }

    public void setSubQuests(List<SubQuest> subQuests) {
        this.subQuests = subQuests;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public void setIsDeleted(boolean deleted) {
        this.isDeleted = deleted;
    }

    @Override
    public boolean getIsDeleted() {
        return isDeleted;
    }

    public Recurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public SourceMapping getSourceMapping() {
        return sourceMapping;
    }

    public void setSourceMapping(SourceMapping sourceMapping) {
        this.sourceMapping = sourceMapping;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public boolean isFlexible() {
        return getRecurrence().isFlexible();
    }
}