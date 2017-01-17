package io.ipoli.android.quest.data;

import android.support.v4.util.Pair;

import com.google.firebase.database.Exclude;

import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;

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
import static io.ipoli.android.app.utils.DateUtils.isSameDay;
import static io.ipoli.android.app.utils.DateUtils.isTodayUTC;
import static io.ipoli.android.app.utils.DateUtils.nowUTC;
import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

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

    private Integer duration;
    private List<Reminder> reminders;
    private List<SubQuest> subQuests;

    private Recurrence recurrence;

    private List<Note> notes;

    private String challengeId;

    private Integer timesADay;

    private String source;

    private SourceMapping sourceMapping;

    private Map<String, Boolean> scheduledPeriodEndDates;

    @Exclude
    private String previousChallengeId;

    private Map<String, QuestData> questsData;

    public RepeatingQuest() {
    }

    public RepeatingQuest(String rawText) {
        this.rawText = rawText;
        setCreatedAt(nowUTC().getTime());
        setUpdatedAt(nowUTC().getTime());
        this.category = Category.PERSONAL.name();
        setTimesADay(1);
        this.source = Constants.API_RESOURCE_SOURCE;
    }

    @Exclude
    public Time getStartTime() {
        if (getStartMinute() < 0) {
            return null;
        }
        return Time.of(getStartMinute());
    }

    public void setStartTime(Time time) {
        if (time != null) {
            setStartMinute(time.toMinutesAfterMidnight());
        } else {
            setStartMinute(null);
        }
    }

    public void setScheduledPeriodEndDates(Map<String, Boolean> scheduledPeriodEndDates) {
        this.scheduledPeriodEndDates = scheduledPeriodEndDates;
    }

    @Exclude
    public int getStreak() {
        Recurrence recurrence = getRecurrence();
        List<QuestData> questsData = new ArrayList<>(getQuestsData().values());
        Collections.sort(questsData, (q1, q2) -> -Long.compare(q1.getOriginalScheduledDate(), q2.getOriginalScheduledDate()));
        if (isFlexible()) {
            return getFlexibleStreak(recurrence.getRecurrenceType(), questsData);
        } else {
            return getFixedStreak(questsData);
        }
    }

    @Exclude
    public int getFrequency() {
        Recurrence recurrence = getRecurrence();
        if (recurrence.isFlexible()) {
            return recurrence.getFlexibleCount();
        }
        if (recurrence.getRecurrenceType() == Recurrence.RecurrenceType.DAILY) {
            return 7;
        }
        if (recurrence.getRecurrenceType() == Recurrence.RecurrenceType.MONTHLY) {
            return 1;
        }
        try {
            Recur recur = new Recur(recurrence.getRrule());
            return recur.getDayList().size();
        } catch (ParseException e) {
            return 0;
        }
    }

    private int getFixedStreak(List<QuestData> questsData) {
        int streak = 0;
        for (QuestData qd : questsData) {
            if (new Date(qd.getOriginalScheduledDate()).after(toStartOfDayUTC(LocalDate.now()))) {
                continue;
            }

            if (isTodayUTC(new Date(qd.getOriginalScheduledDate())) && !qd.isComplete()) {
                continue;
            }

            if (qd.isComplete() && isSameDay(new Date(qd.getScheduledDate()), new Date(qd.getOriginalScheduledDate()))) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }


    private int getFlexibleStreak(Recurrence.RecurrenceType recurrenceType, List<QuestData> questsData) {
        int streak = 0;
        for (QuestData qd : questsData) {

            if (new Date(qd.getOriginalScheduledDate()).after(DateUtils.toStartOfDayUTC(LocalDate.now()))) {
                continue;
            }

            if (isTodayUTC(new Date(qd.getOriginalScheduledDate())) && !qd.isComplete()) {
                continue;
            }

            LocalDate periodStart = null;
            LocalDate periodEnd = null;
            if (recurrenceType == Recurrence.RecurrenceType.MONTHLY) {
                periodStart = new LocalDate(qd.getOriginalScheduledDate()).dayOfMonth().withMinimumValue();
                periodEnd = periodStart.dayOfMonth().withMaximumValue();
            } else {
                periodStart = new LocalDate(qd.getOriginalScheduledDate()).dayOfWeek().withMinimumValue();
                periodEnd = periodStart.dayOfWeek().withMaximumValue();
            }

            if (qd.isComplete()
                    && isBetween(new Date(qd.getScheduledDate()), toStartOfDayUTC(periodStart), toStartOfDayUTC(periodEnd))) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    @Exclude
    public Date getNextScheduledDate(LocalDate currentDate) {
        Date nextDate = null;
        for (QuestData qd : getQuestsData().values()) {
            if (!qd.isComplete() && qd.getScheduledDate() != null && qd.getScheduledDate() >= DateUtils.toStartOfDayUTC(currentDate).getTime()) {
                if (nextDate == null || nextDate.getTime() > qd.getScheduledDate()) {
                    nextDate = new Date(qd.getScheduledDate());
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
        setPreviousChallengeId(this.challengeId);
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

    @Exclude
    public void addScheduledPeriodEndDate(Date date) {
        getScheduledPeriodEndDates().put(String.valueOf(date.getTime()), true);
    }

    @Exclude
    public boolean isFlexible() {
        return getRecurrence().isFlexible();
    }

    public Map<String, Boolean> getScheduledPeriodEndDates() {
        if (scheduledPeriodEndDates == null) {
            scheduledPeriodEndDates = new HashMap<>();
        }
        return scheduledPeriodEndDates;
    }

    @Exclude
    public boolean shouldBeScheduledForPeriod(Date periodEnd) {
        return !getScheduledPeriodEndDates().containsKey(String.valueOf(periodEnd.getTime()));
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

    public List<PeriodHistory> getPeriodHistories(LocalDate currentDate) {
        List<PeriodHistory> result = new ArrayList<>();
        int frequency = getFrequency();
        List<Pair<LocalDate, LocalDate>> pairs = recurrence.getRecurrenceType() == Recurrence.RecurrenceType.MONTHLY ?
                DateUtils.getBoundsFor4MonthsInThePast(currentDate) :
                DateUtils.getBoundsFor4WeeksInThePast(currentDate);

        for (Pair<LocalDate, LocalDate> p : pairs) {
            result.add(new PeriodHistory(toStartOfDayUTC(p.first).getTime(), toStartOfDayUTC(p.second).getTime(), frequency));
        }

        for (QuestData qd : getQuestsData().values()) {
            for (PeriodHistory p : result) {
                Long scheduledDate = qd.getScheduledDate();
                if (scheduledDate == null) {
                    continue;
                }
                if (DateUtils.isBetween(new Date(scheduledDate), new Date(p.getStart()), new Date(p.getEnd()))) {
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

    public Map<String, QuestData> getQuestsData() {
        if (questsData == null) {
            questsData = new HashMap<>();
        }
        return questsData;
    }

    public void setQuestsData(Map<String, QuestData> questsData) {
        this.questsData = questsData;
    }

    @Exclude
    public void addQuestData(String id, QuestData questData) {
        getQuestsData().put(id, questData);
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
    public int getTotalTimeSpent() {
        int timeSpent = 0;
        for (QuestData questData : getQuestsData().values()) {
            if (questData.isComplete()) {
                timeSpent += questData.getDuration();
            }
        }
        return timeSpent;
    }

    @Exclude
    public boolean isScheduledForDate(LocalDate date) {
        for (String dateString : getScheduledPeriodEndDates().keySet()) {
            LocalDate periodEnd = new LocalDate(Long.valueOf(dateString));

            if (date.isBefore(periodEnd) || date.equals(periodEnd)) {
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

    @Exclude
    public Category getCategoryType() {
        return Category.valueOf(getCategory());
    }

    @Exclude
    public void setCategoryType(Category category) {
        setCategory(category.name());
    }

    public void addReminder(Reminder reminder) {
        getReminders().add(reminder);
    }

    @Exclude
    public void setStartTimePreference(TimePreference timePreference) {
        if (timePreference != null) {
            this.preferredStartTime = timePreference.name();
        }
    }

    @Exclude
    public TimePreference getStartTimePreference() {
        if (StringUtils.isEmpty(preferredStartTime)) {
            return TimePreference.ANY;
        }
        return TimePreference.valueOf(preferredStartTime);
    }
}
