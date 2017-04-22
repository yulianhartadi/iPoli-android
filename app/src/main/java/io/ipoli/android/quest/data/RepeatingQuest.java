package io.ipoli.android.quest.data;

import android.support.v4.util.Pair;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.fortuna.ical4j.model.Recur;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.note.data.Note;
import io.ipoli.android.reminder.data.Reminder;

import static io.ipoli.android.app.utils.DateUtils.isBetween;
import static io.ipoli.android.app.utils.DateUtils.nowUTC;
import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/26/16.
 */
public class RepeatingQuest extends PersistedObject implements BaseQuest {

    public static final String TYPE = "repeatingQuest";
    private String rawText;

    private String name;

    private String category;

    private boolean allDay;

    private Integer priority;

    private Integer startMinute;

    private String preferredStartTime;

    private Integer duration;
    private List<Reminder> reminders;
    private List<SubQuest> subQuests;

    private Long completedAt;

    private Recurrence recurrence;

    private List<Note> notes;

    private String challengeId;

    private Integer timesADay;

    private String source;

    private SourceMapping sourceMapping;

    private Map<String, Boolean> scheduledPeriodEndDates;

    @JsonIgnore
    private Map<String, QuestData> questsData;

    public RepeatingQuest() {
        super(TYPE);
    }

    public RepeatingQuest(String rawText) {
        super(TYPE);
        this.rawText = rawText;
        setCreatedAt(nowUTC().getTime());
        setUpdatedAt(nowUTC().getTime());
        this.category = Category.PERSONAL.name();
        setTimesADay(1);
        this.source = Constants.API_RESOURCE_SOURCE;
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

    public void setScheduledPeriodEndDates(Map<String, Boolean> scheduledPeriodEndDates) {
        this.scheduledPeriodEndDates = scheduledPeriodEndDates;
    }


    @JsonIgnore
    public Date getCompletedAtDate() {
        return completedAt != null ? new Date(completedAt) : null;
    }


    @JsonIgnore
    public void setCompletedAtDate(Date completedAtDate) {
        completedAt = completedAtDate != null ? completedAtDate.getTime() : null;
    }


    @JsonIgnore
    public boolean isCompleted() {
        return getCompletedAtDate() != null;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    @JsonIgnore
    public boolean shouldBeScheduledAfter(LocalDate date) {
        return getRecurrence().getDtendDate() == null ||
                getRecurrence().getDtend() >= toStartOfDayUTC(date).getTime();
    }

    @JsonIgnore
    public int getStreak() {
        Recurrence recurrence = getRecurrence();
        List<QuestData> questsData = new ArrayList<>(getQuestsData().values());
        Collections.sort(questsData,
                (o1, o2) -> -o1.getOriginalScheduledDate().compareTo(o2.getOriginalScheduledDate()));
        if (isFlexible()) {
            return getFlexibleStreak(recurrence.getRecurrenceType(), questsData);
        } else {
            return getFixedStreak(questsData);
        }
    }

    @JsonIgnore
    public int getFrequency() {
        Recurrence recurrence = getRecurrence();
        if (recurrence.isFlexible()) {
            return recurrence.getFlexibleCount();
        }
        if (recurrence.getRecurrenceType() == Recurrence.RepeatType.DAILY) {
            return 7;
        }
        if (recurrence.getRecurrenceType() == Recurrence.RepeatType.MONTHLY) {
            return 1;
        }
        try {
            Recur recur = new Recur(recurrence.getRrule());
            return recur.getDayList().size();
        } catch (ParseException e) {
            return 0;
        }
    }

    @JsonIgnore
    private int getFixedStreak(List<QuestData> questsData) {
        int streak = 0;
        for (QuestData qd : questsData) {
            if (qd.getOriginalScheduledDate().isAfter(LocalDate.now())) {
                continue;
            }

            if (DateUtils.isToday(qd.getOriginalScheduledDate()) && !qd.isComplete()) {
                continue;
            }

            if (qd.isComplete() && qd.getScheduledDate().isEqual(qd.getOriginalScheduledDate())) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    @JsonIgnore
    private int getFlexibleStreak(Recurrence.RepeatType repeatType, List<QuestData> questsData) {
        int streak = 0;
        for (QuestData qd : questsData) {

            LocalDate originalScheduledDate = qd.getOriginalScheduledDate();
            if (originalScheduledDate.isAfter(LocalDate.now())) {
                continue;
            }

            if (DateUtils.isToday(originalScheduledDate) && !qd.isComplete()) {
                continue;
            }

            LocalDate periodStart;
            LocalDate periodEnd;
            if (repeatType == Recurrence.RepeatType.MONTHLY) {
                periodStart = originalScheduledDate.with(TemporalAdjusters.firstDayOfMonth());
                periodEnd = periodStart.with(TemporalAdjusters.lastDayOfMonth());
            } else {
                periodStart = originalScheduledDate.with(DayOfWeek.MONDAY);
                periodEnd = periodStart.with(DayOfWeek.SUNDAY);
            }

            if (qd.isComplete()
                    && isBetween(qd.getScheduledDate(), periodStart, periodEnd)) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    @JsonIgnore
    public LocalDate getNextScheduledDate(LocalDate currentDate) {
        LocalDate nextDate = null;
        for (QuestData qd : getQuestsData().values()) {
            if (!qd.isComplete() && qd.getScheduledDate() != null && !currentDate.isAfter(qd.getScheduledDate())) {
                if (nextDate == null || nextDate.isAfter(qd.getScheduledDate())) {
                    nextDate = qd.getScheduledDate();
                }
            }
        }
        return nextDate;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getStartMinute() {
        return startMinute;
    }

    public List<SubQuest> getSubQuests() {
        return subQuests;
    }

    public void setSubQuests(List<SubQuest> subQuests) {
        this.subQuests = subQuests;
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
        return priority != null ? priority : Quest.PRIORITY_NOT_IMPORTANT_NOT_URGENT;
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

    @JsonIgnore
    public void addScheduledPeriodEndDate(LocalDate date) {
        getScheduledPeriodEndDates().put(String.valueOf(DateUtils.toMillis(date)), true);
    }

    @JsonIgnore
    public boolean isFlexible() {
        return getRecurrence().isFlexible();
    }

    public Map<String, Boolean> getScheduledPeriodEndDates() {
        if (scheduledPeriodEndDates == null) {
            scheduledPeriodEndDates = new HashMap<>();
        }
        return scheduledPeriodEndDates;
    }

    @JsonIgnore
    public boolean shouldBeScheduledForPeriod(LocalDate periodEnd) {
        return !getScheduledPeriodEndDates().containsKey(String.valueOf(DateUtils.toMillis(periodEnd)));
    }

    @JsonIgnore
    public List<Note> getTextNotes() {
        List<Note> textNotes = new ArrayList<>();
        for (Note note : getNotes()) {
            if (note.getNoteType().equals(Note.NoteType.TEXT.name())) {
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
    public List<PeriodHistory> getPeriodHistories(LocalDate currentDate) {
        List<PeriodHistory> result = new ArrayList<>();
        int frequency = getFrequency();
        List<Pair<LocalDate, LocalDate>> pairs = recurrence.getRecurrenceType() == Recurrence.RepeatType.MONTHLY ?
                DateUtils.getBoundsFor4MonthsInThePast(currentDate) :
                DateUtils.getBoundsFor4WeeksInThePast(currentDate);

        for (Pair<LocalDate, LocalDate> p : pairs) {
            result.add(new PeriodHistory(p.first, p.second, frequency));
        }

        for (QuestData qd : getQuestsData().values()) {
            for (PeriodHistory p : result) {
                LocalDate scheduledDate = qd.getScheduledDate();
                if (scheduledDate == null) {
                    continue;
                }
                if (DateUtils.isBetween(scheduledDate, p.getStartDate(), (p.getEndDate()))) {
                    if (qd.isComplete()) {
                        p.increaseCompletedCount();
                    }
                    p.increaseScheduledCount();
                    break;
                }
            }
        }

        return result;
    }

    @JsonIgnore
    public Map<String, QuestData> getQuestsData() {
        if (questsData == null) {
            questsData = new HashMap<>();
        }
        return questsData;
    }

    @JsonIgnore
    public void setQuestsData(Map<String, QuestData> questsData) {
        this.questsData = questsData;
    }


    @JsonIgnore
    public void addQuestData(String id, QuestData questData) {
        getQuestsData().put(id, questData);
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
    public boolean isScheduledForDate(LocalDate date) {
        for (String dateString : getScheduledPeriodEndDates().keySet()) {
            LocalDate periodEnd = DateUtils.fromMillis(Long.valueOf(dateString));

            if (date.isBefore(periodEnd) || date.isEqual(periodEnd)) {
                return true;
            }
        }
        return false;
    }

    public void setTimesADay(int timesADay) {
        this.timesADay = timesADay;
    }

    public Integer getTimesADay() {
        return timesADay;
    }

    @JsonIgnore
    public Category getCategoryType() {
        return Category.valueOf(getCategory());
    }

    @JsonIgnore
    public void setCategoryType(Category category) {
        setCategory(category.name());
    }

    public void addReminder(Reminder reminder) {
        getReminders().add(reminder);
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
}
