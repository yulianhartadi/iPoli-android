package io.ipoli.android.quest.data;

import com.google.firebase.database.Exclude;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.note.data.Note;
import io.ipoli.android.quest.generators.RewardProvider;
import io.ipoli.android.reminder.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class Quest extends PersistedObject implements RewardProvider, BaseQuest {

    public static final int PRIORITY_MOST_IMPORTANT_FOR_DAY = 4;
    public static final int DEFAULT_NO_PRIORITY_VALUE = -1;

    private String rawText;

    private String name;

    private String category;

    private boolean allDay;

    private Integer priority;

    private Integer startMinute;

    private String preferredStartTime;
    private Boolean flexibleStartTime;

    private Integer duration;

    private Long start;
    private Long originalStart;
    private Long end;

    private String repeatingQuestId;

    private List<Reminder> reminders;
    private List<SubQuest> subQuests;
    private List<Long> reminderStartTimes;
    private Integer difficulty;

    private Long completedAt;
    private Integer completedAtMinute;

    private Long actualStart;

    private String challengeId;

    private Long coins;
    private Long experience;

    private List<Note> notes;

    private Integer timesADay;

    private String source;

    private SourceMapping sourceMapping;

    @Exclude
    private Long previousScheduledDate;

    @Exclude
    private String previousChallengeId;

    @Exclude
    private transient boolean isPlaceholder;
    private Integer completedCount;

    public Quest() {
    }

    public Quest(String name) {
        this(name, null);
    }

    public Quest(String name, Date endDate) {
        this.name = name;
        setEndDateFromLocal(endDate);
        setStartDateFromLocal(endDate);
        setOriginalStartDate(endDate);
        setStartMinute(null);
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
        this.category = Category.PERSONAL.name();
        this.flexibleStartTime = false;
        this.source = Constants.API_RESOURCE_SOURCE;
        this.setCompletedCount(0);
        this.setTimesADay(1);
    }

    @Exclude
    public Category getCategoryType() {
        return Category.valueOf(getCategory());
    }

    @Exclude
    public boolean isCompleted() {
        return getCompletedAtDate() != null;
    }

    @Exclude
    public Long getPreviousScheduledDate() {
        return previousScheduledDate;
    }

    @Exclude
    public void setPreviousScheduledDate(Long previousScheduledDate) {
        this.previousScheduledDate = previousScheduledDate;
    }

    public void setDuration(Integer duration) {
        if (duration == null) {
            this.duration = null;
            return;
        }
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

    public List<SubQuest> getSubQuests() {
        if (subQuests == null) {
            subQuests = new ArrayList<>();
        }
        return subQuests;
    }

    public void setSubQuests(List<SubQuest> subQuests) {
        this.subQuests = subQuests;
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

    public String getRepeatingQuestId() {
        return repeatingQuestId;
    }

    public void setRepeatingQuestId(String repeatingQuestId) {
        this.repeatingQuestId = repeatingQuestId;
    }

    @Exclude
    public void addReminderStartTime(long startTime) {
        getReminderStartTimes().add(startTime);
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public int getPriority() {
        return priority != null ? priority : DEFAULT_NO_PRIORITY_VALUE;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Exclude
    public Date getStartDate() {
        return start != null ? new Date(start) : null;
    }

    @Exclude
    public void setStartDate(Date startDate) {
        start = startDate != null ? startDate.getTime() : null;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    @Exclude
    public void setStartDateFromLocal(Date startDate) {
        setStartDate(DateUtils.getDate(startDate));
    }

    @Exclude
    public void setOriginalStartDate(Date originalStartDate) {
        originalStart = originalStartDate != null ? originalStartDate.getTime() : null;
    }

    @Exclude
    public Date getOriginalStartDate() {
        return originalStart != null ? new Date(originalStart) : null;
    }

    public Long getOriginalStart() {
        return originalStart;
    }

    public void setOriginalStart(Long originalStart) {
        this.originalStart = originalStart;
    }

    @Exclude
    public Date getEndDate() {
        return end != null ? new Date(end) : null;
    }

    @Exclude
    public void setEndDate(Date endDate) {
        setEnd(endDate != null ? endDate.getTime() : null);
    }

    @Exclude
    public void setEndDateFromLocal(Date endDate) {
        setEndDate(DateUtils.getDate(endDate));
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        setPreviousScheduledDate(this.end);
        this.end = end;
    }

    public static Time getStartTime(Quest quest) {
        if (quest.getStartMinute() < 0) {
            return null;
        }
        return Time.of(quest.getStartMinute());
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public Long getUpdatedAt() {
        return updatedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public static Date getStartDateTime(Quest quest) {
        if (quest.getStartMinute() < 0 || quest.getEndDate() == null) {
            return null;
        }
        Time startTime = Time.of(quest.getStartMinute());
        return new LocalDate(quest.getEndDate(), DateTimeZone.UTC).toDateTime(new LocalTime(startTime.getHours(), startTime.getMinutes())).toDate();
    }

    @Exclude
    public Date getActualStartDate() {
        return actualStart != null ? new Date(actualStart) : null;
    }

    @Exclude
    public void setActualStartDate(Date actualStartDate) {
        actualStart = actualStartDate != null ? actualStartDate.getTime() : null;
    }

    public Long getActualStart() {
        return actualStart;
    }

    public void setActualStart(Long actualStart) {
        this.actualStart = actualStart;
    }

    public Integer getCompletedAtMinute() {
        return completedAtMinute;
    }

    public void setCompletedAtMinute(Integer completedAtMinute) {
        this.completedAtMinute = completedAtMinute;
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

    public static boolean isStarted(Quest quest) {
        return quest.getActualStartDate() != null && quest.getCompletedAtDate() == null;
    }

    public static void setStartTime(Quest quest, Time time) {
        if (time != null) {
            quest.setStartMinute(time.toMinutesAfterMidnight());
        } else {
            quest.setStartMinute(null);
        }
    }

    @Exclude
    public boolean isScheduled() {
        return getEndDate() != null && getStartMinute() >= 0;
    }

    @Exclude
    public boolean isScheduledForToday() {
        return isScheduledFor(new LocalDate());
    }

    @Exclude
    public boolean isScheduledFor(LocalDate date) {
        return date.isEqual(new LocalDate(getEndDate(), DateTimeZone.UTC));
    }

    @Exclude
    public boolean isScheduledForThePast() {
        return getEndDate() != null && getEndDate().before(DateUtils.toStartOfDayUTC(LocalDate.now()));
    }

    public int getStartMinute() {
        return startMinute != null ? startMinute : -1;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Exclude
    public boolean isStarted() {
        return actualStart != null && completedAt == null;
    }

    @Exclude
    public boolean isScheduledForTomorrow() {
        return DateUtils.isTomorrowUTC(DateUtils.toStartOfDayUTC(new LocalDate(getEndDate(), DateTimeZone.UTC)));
    }

    @Exclude
    public boolean isFromRepeatingQuest() {
        return !StringUtils.isEmpty(getRepeatingQuestId());
    }

    @Exclude
    public boolean isFromChallenge() {
        return !StringUtils.isEmpty(getChallengeId());
    }

    public SourceMapping getSourceMapping() {
        return sourceMapping;
    }

    public void setSourceMapping(SourceMapping sourceMapping) {
        this.sourceMapping = sourceMapping;
    }

    public Long getCoins() {
        return coins;
    }

    public Long getExperience() {
        return experience;
    }

    public void setCoins(Long coins) {
        this.coins = coins;
    }

    public void setExperience(Long experience) {
        this.experience = experience;
    }

    public void setFlexibleStartTime(Boolean flexibleStartTime) {
        this.flexibleStartTime = flexibleStartTime;
    }

    public Boolean getFlexibleStartTime() {
        return flexibleStartTime;
    }

    @Exclude
    public boolean isPlaceholder() {
        return isPlaceholder;
    }

    @Exclude
    public void setPlaceholder(boolean placeholder) {
        isPlaceholder = placeholder;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        setPreviousChallengeId(this.challengeId);
        this.challengeId = challengeId;
    }

    @Exclude
    public int getActualDuration() {
        if (this.isCompleted() && getActualStartDate() != null) {
            return (int) TimeUnit.MILLISECONDS.toMinutes(getCompletedAtDate().getTime() - getActualStartDate().getTime());
        }
        return getDuration();
    }

    @Exclude
    public int getActualStartMinute() {
        if (this.isCompleted() && getActualStartDate() != null) {
            return Math.max(0, getCompletedAtMinute() - getActualDuration());
        }
        return getStartMinute();
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

    @Exclude
    public void addSubQuest(SubQuest subQuest) {
        getSubQuests().add(subQuest);
    }

    @Exclude
    public void removeSubQuest(SubQuest subQuest) {
        getSubQuests().remove(subQuest);
    }

    @Exclude
    public boolean shouldNotBeReminded() {
        return getActualStart() != null || getCompletedAt() != null;
    }

    public List<Long> getReminderStartTimes() {
        if (reminderStartTimes == null) {
            reminderStartTimes = new ArrayList<>();
        }
        return reminderStartTimes;
    }

    public void setReminderStartTimes(List<Long> reminderStartTimes) {
        this.reminderStartTimes = reminderStartTimes;
    }

    public Integer getTimesADay() {
        return timesADay;
    }

    public void setTimesADay(Integer timesADay) {
        setCompletedCount(Math.min(timesADay, getCompletedCount()));
        this.timesADay = timesADay;
    }

    @Exclude
    public String getPreviousChallengeId() {
        return previousChallengeId;
    }

    @Exclude
    public void setPreviousChallengeId(String previousChallengeId) {
        this.previousChallengeId = previousChallengeId;
    }

    @Exclude
    public boolean hasStartTime() {
        return getStartMinute() >= 0;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    @Exclude
    public int getRemainingCount() {
        return getTimesADay() - getCompletedCount();
    }

    @Exclude
    public void increaseCompletedCount() {
        completedCount++;
    }

    @Exclude
    public boolean completedAllTimesForDay() {
        return getRemainingCount() == 0;
    }

    @Exclude
    public boolean shouldBeDoneMultipleTimesPerDay() {
        return getTimesADay() > 1;
    }
}