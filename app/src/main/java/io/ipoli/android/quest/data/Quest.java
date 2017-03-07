package io.ipoli.android.quest.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.note.data.Note;
import io.ipoli.android.quest.generators.RewardProvider;
import io.ipoli.android.reminder.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class Quest extends PersistedObject implements RewardProvider, BaseQuest {

    public static final String TYPE = "quest";

    public static final int PRIORITY_MOST_IMPORTANT_FOR_DAY = 4;
    public static final int PRIORITY_IMPORTANT_URGENT = 3;
    public static final int PRIORITY_IMPORTANT_NOT_URGENT = 2;
    public static final int PRIORITY_NOT_IMPORTANT_URGENT = 1;
    public static final int PRIORITY_NOT_IMPORTANT_NOT_URGENT = 0;

    private String rawText;

    private String name;

    private String category;

    private Boolean allDay;

    private Integer priority;

    private Integer startMinute;

    private String preferredStartTime;

    private Integer duration;

    private Long start;
    private Long end;

    @JsonProperty
    private Long scheduled;

    private Long originalScheduled;

    private String repeatingQuestId;

    private List<Reminder> reminders;
    private List<SubQuest> subQuests;
    private List<Long> reminderStartTimes;
    private Integer difficulty;

    private Long completedAt;
    private Integer completedAtMinute;

    private Long actualStart;

    private String challengeId;

    private String playerId;

    private Long coins;
    private Long experience;

    private List<Note> notes;

    private Integer timesADay;

    private String source;

    private SourceMapping sourceMapping;

    private Integer completedCount;


    @JsonIgnore
    private Long previousScheduledDate;


    @JsonIgnore
    private String previousChallengeId;


    @JsonIgnore
    private transient boolean isPlaceholder;

    public Quest() {
    }

    public Quest(String name) {
        this(name, null);
    }

    public Quest(String name, Date endDate) {
        this.name = name;
        setEndDateFromLocal(endDate);
        setStartDateFromLocal(endDate);
        setScheduledDateFromLocal(endDate);
        setStartMinute(null);
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
        this.category = Category.PERSONAL.name();
        this.source = Constants.API_RESOURCE_SOURCE;
        this.setCompletedCount(0);
        this.setTimesADay(1);
        this.allDay = false;
        type = TYPE;
    }

    public Long getOriginalScheduled() {
        return originalScheduled;
    }

    public void setOriginalScheduled(Long originalScheduled) {
        this.originalScheduled = originalScheduled;
    }

    public Long getScheduled() {
        return scheduled;
    }

    public void setScheduled(Long scheduled) {
        this.scheduled = scheduled;
    }


    @JsonIgnore
    public void setOriginalScheduledDate(Date originalScheduledDate) {
        originalScheduled = originalScheduledDate != null ? originalScheduledDate.getTime() : null;
    }


    @JsonIgnore
    public Date getOriginalScheduledDate() {
        return originalScheduled != null ? new Date(originalScheduled) : null;
    }


    @JsonIgnore
    public void setScheduledDate(Date scheduledDate) {
        previousScheduledDate = scheduled;
        setScheduled(scheduledDate != null ? scheduledDate.getTime() : null);
        if(getOriginalScheduledDate() == null) {
            setOriginalScheduledDate(getScheduledDate());
        }
    }


    @JsonIgnore
    public Date getScheduledDate() {
        return scheduled != null ? new Date(scheduled) : null;
    }


    @JsonIgnore
    public Time getStartTime() {
        if (getStartMinute() == null) {
            return null;
        }
        return Time.of(getStartMinute());
    }


    @JsonIgnore
    public void setStartTime(Time time) {
        if (time != null) {
            setStartMinute(time.toMinuteOfDay());
        } else {
            setStartMinute(null);
        }
    }


    @JsonIgnore
    public Category getCategoryType() {
        return Category.valueOf(getCategory());
    }


    @JsonIgnore
    public boolean isCompleted() {
        return getCompletedAtDate() != null;
    }


    @JsonIgnore
    public Long getPreviousScheduledDate() {
        return previousScheduledDate;
    }


    @JsonIgnore
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


    @JsonIgnore
    public void addReminderStartTime(long startTime) {
        getReminderStartTimes().add(startTime);
    }

    public Boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public int getPriority() {
        return priority != null ? priority : Quest.PRIORITY_NOT_IMPORTANT_NOT_URGENT;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }


    @JsonIgnore
    public Date getStartDate() {
        return start != null ? new Date(start) : null;
    }


    @JsonIgnore
    public void setStartDate(Date startDate) {
        start = startDate != null ? startDate.getTime() : null;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }


    @JsonIgnore
    public void setStartDateFromLocal(Date startDate) {
        setStartDate(DateUtils.getDate(startDate));
    }


    @JsonIgnore
    public Date getEndDate() {
        return end != null ? new Date(end) : null;
    }


    @JsonIgnore
    public void setEndDate(Date endDate) {
        setEnd(endDate != null ? endDate.getTime() : null);
    }


    @JsonIgnore
    public void setEndDateFromLocal(Date endDate) {
        setEndDate(DateUtils.getDate(endDate));
    }


    @JsonIgnore
    public void setScheduledDateFromLocal(Date scheduledDate) {
        setScheduledDate(DateUtils.getDate(scheduledDate));
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        setPreviousScheduledDate(this.end);
        this.end = end;
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
        if (quest.getStartMinute() == null || quest.getScheduled() == null) {
            return null;
        }
        Time startTime = Time.of(quest.getStartMinute());
        return new LocalDate(quest.getScheduled(), DateTimeZone.UTC).toDateTime(new LocalTime(startTime.getHours(), startTime.getMinutes())).toDate();
    }


    @JsonIgnore
    public Date getActualStartDate() {
        return actualStart != null ? new Date(actualStart) : null;
    }


    @JsonIgnore
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


    @JsonIgnore
    public Date getCompletedAtDate() {
        return completedAt != null ? new Date(completedAt) : null;
    }


    @JsonIgnore
    public void setCompletedAtDateFromLocal(Date completedAtDate) {
        Date date = DateUtils.getDate(completedAtDate);
        setCompletedAt(date == null ? null : date.getTime());
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


    @JsonIgnore
    public boolean isScheduled() {
        return getScheduled() != null && hasStartTime();
    }


    @JsonIgnore
    public boolean isScheduledForToday() {
        return isScheduledFor(new LocalDate());
    }


    @JsonIgnore
    public boolean isScheduledFor(LocalDate date) {
        return date.isEqual(new LocalDate(getScheduled(), DateTimeZone.UTC));
    }


    @JsonIgnore
    public boolean isScheduledForThePast() {
        return getScheduled() != null && getScheduledDate().before(DateUtils.toStartOfDayUTC(LocalDate.now()));
    }

    public Integer getStartMinute() {
        return startMinute;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


    @JsonIgnore
    public boolean isStarted() {
        return actualStart != null && completedAt == null;
    }


    @JsonIgnore
    public boolean isScheduledForTomorrow() {
        if (getScheduledDate() == null) {
            return false;
        }
        return DateUtils.isTomorrowUTC(DateUtils.toStartOfDayUTC(new LocalDate(getScheduledDate(), DateTimeZone.UTC)));
    }


    @JsonIgnore
    public boolean isFromRepeatingQuest() {
        return !StringUtils.isEmpty(getRepeatingQuestId());
    }


    @JsonIgnore
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


    @JsonIgnore
    public boolean isPlaceholder() {
        return isPlaceholder;
    }


    @JsonIgnore
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


    @JsonIgnore
    public int getActualDuration() {
        if (this.isCompleted() && getActualStartDate() != null) {
            long completedAtMillis = TimeUnit.MINUTES.toMillis(getCompletedAtMinute());
            long completedAtDateMillis = new LocalDate(getCompletedAt(), DateTimeZone.UTC).toDate().getTime();
            return (int) TimeUnit.MILLISECONDS.toMinutes(completedAtDateMillis + completedAtMillis - actualStart);
        }
        return getDuration();
    }


    @JsonIgnore
    public Integer getActualStartMinute() {
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


    @JsonIgnore
    public List<Note> getTextNotes() {
        List<Note> textNotes = new ArrayList<>();
        for (Note note : getNotes()) {
            if (note.getType().equals(Note.NoteType.TEXT.name())) {
                textNotes.add(note);
            }
        }
        return textNotes;
    }

    public void addNote(Note note) {
        getNotes().add(note);
    }


    @JsonIgnore
    public void removeTextNote() {
        List<Note> txtNotes = getTextNotes();
        getNotes().removeAll(txtNotes);
    }


    @JsonIgnore
    public void addSubQuest(SubQuest subQuest) {
        getSubQuests().add(subQuest);
    }


    @JsonIgnore
    public void removeSubQuest(SubQuest subQuest) {
        getSubQuests().remove(subQuest);
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


    @JsonIgnore
    public String getPreviousChallengeId() {
        return previousChallengeId;
    }


    @JsonIgnore
    public void setPreviousChallengeId(String previousChallengeId) {
        this.previousChallengeId = previousChallengeId;
    }


    @JsonIgnore
    public boolean hasStartTime() {
        return getStartMinute() != null;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }


    @JsonIgnore
    public int getRemainingCount() {
        return getTimesADay() - getCompletedCount();
    }


    @JsonIgnore
    public void increaseCompletedCount() {
        completedCount++;
    }


    @JsonIgnore
    public boolean completedAllTimesForDay() {
        return getRemainingCount() == 0;
    }


    @JsonIgnore
    public boolean shouldBeDoneMultipleTimesPerDay() {
        return getTimesADay() > 1;
    }


    @JsonIgnore
    public void setCategoryType(Category category) {
        this.category = category.name();
    }


    @JsonIgnore
    public void addReminder(Reminder reminder) {
        getReminders().add(reminder);
    }

    public String getPreferredStartTime() {
        return preferredStartTime;
    }

    public void setPreferredStartTime(String preferredStartTime) {
        this.preferredStartTime = preferredStartTime;
    }


    @JsonIgnore
    public void setStartTimePreference(TimePreference timePreference) {
        if (timePreference != null) {
            this.preferredStartTime = timePreference.name();
        }
    }


    @JsonIgnore
    public TimePreference getStartTimePreference() {
        if (StringUtils.isEmpty(preferredStartTime)) {
            return TimePreference.ANY;
        }
        return TimePreference.valueOf(preferredStartTime);
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}