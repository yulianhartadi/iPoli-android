package io.ipoli.android.quest.data;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.note.data.Note;
import io.ipoli.android.reminder.data.Reminder;

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

    private List<Note> notes;

    private String challengeId;

    private String source;

    private SourceMapping sourceMapping;

    private Map<String, Boolean> scheduledPeriodEndDates;

    private Long totalMinutesSpent;
    private Long streak;
    private Long nextScheduledDate;
    private Map<Long, Boolean> scheduledDates;

    // In chronological order
    private List<PeriodHistory> periodHistories;

    public RepeatingQuest() {
    }

    public void setScheduledPeriodEndDates(Map<String, Boolean> scheduledPeriodEndDates) {
        this.scheduledPeriodEndDates = scheduledPeriodEndDates;
    }

    public Long getTotalMinutesSpent() {
        return totalMinutesSpent;
    }

    public void setTotalMinutesSpent(Long totalMinutesSpent) {
        this.totalMinutesSpent = totalMinutesSpent;
    }

    public Long getStreak() {
        return streak;
    }

    public void setStreak(Long streak) {
        this.streak = streak;
    }

    public Long getNextScheduledDate() {
        return nextScheduledDate;
    }

    public void setNextScheduledDate(Long nextScheduledDate) {
        this.nextScheduledDate = nextScheduledDate;
    }

    public Map<Long, Boolean> getScheduledDates() {
        if (scheduledDates == null) {
            scheduledDates = new HashMap<>();
        }
        return scheduledDates;
    }

    public void setScheduledDates(Map<Long, Boolean> scheduledDates) {
        this.scheduledDates = scheduledDates;
    }

    public void setDuration(Integer duration) {
        this.duration = (int) Math.min(TimeUnit.HOURS.toMinutes(Constants.MAX_QUEST_DURATION_HOURS), duration);
    }

    public List<Reminder> getReminders() {
        if (reminders == null) {
            reminders = new ArrayList<>();
        }
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
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
        this.category = Category.PERSONAL.name();
        this.flexibleStartTime = false;
        this.source = Constants.API_RESOURCE_SOURCE;
        this.streak = 0L;
        this.totalMinutesSpent = 0L;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
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

    public List<Note> getNotes() {
        if (notes == null) {
            notes = new ArrayList<>();
        }
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getPreferredStartTime() {
        return preferredStartTime;
    }

    public void setPreferredStartTime(String preferredStartTime) {
        this.preferredStartTime = preferredStartTime;
    }

    public Boolean getFlexibleStartTime() {
        return flexibleStartTime;
    }

    public void setFlexibleStartTime(Boolean flexibleStartTime) {
        this.flexibleStartTime = flexibleStartTime;
    }

    @Exclude
    public void addScheduledPeriodEndDate(Date date) {
        if (scheduledPeriodEndDates == null) {
            scheduledPeriodEndDates = new HashMap<>();
        }
        scheduledPeriodEndDates.put(String.valueOf(date.getTime()), true);
    }

    @Exclude
    public boolean isFlexible() {
        return getRecurrence().isFlexible();
    }

    public static Category getCategory(RepeatingQuest repeatingQuest) {
        return Category.valueOf(repeatingQuest.getCategory());
    }

    public Map<String, Boolean> getScheduledPeriodEndDates() {
        if (scheduledPeriodEndDates == null) {
            return new HashMap<>();
        }
        return scheduledPeriodEndDates;
    }

    @Exclude
    public boolean shouldBeScheduledForPeriod(Date periodEnd) {
        if (scheduledPeriodEndDates == null) {
            return true;
        }
        return !scheduledPeriodEndDates.containsKey(String.valueOf(periodEnd.getTime()));
    }

    @Exclude
    public List<Note> getTextNotes() {
        List<Note> textNotes = new ArrayList<>();
        for (Note note : getNotes()) {
            if (note.getType().equals(Note.Type.TEXT.name())) {
                textNotes.add(note);
            }
        }
        return textNotes;
    }

    public void addNote(Note note) {
        getNotes().add(note);
    }

    @Exclude
    public void removeTextNote() {
        List<Note> txtNotes = getTextNotes();
        getNotes().removeAll(txtNotes);
    }

    public List<PeriodHistory> getPeriodHistories() {
        if (periodHistories == null) {
            periodHistories = new ArrayList<>();
        }
        return periodHistories;
    }

    public void setPeriodHistories(List<PeriodHistory> periodHistories) {
        this.periodHistories = periodHistories;
    }

}