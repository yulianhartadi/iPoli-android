package io.ipoli.android.quest.data;

import android.text.TextUtils;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.net.RemoteObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestContext;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class Quest extends RealmObject implements RemoteObject<Quest> {

    @Required
    @PrimaryKey
    private String id;

    private String rawText;

    @Required
    private String name;

    private String context;

    private boolean allDay;

    private Integer priority;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    private Integer startMinute;

    private String preferredStartTime;
    private Boolean flexibleStartTime;

    private Integer duration;
    private Date startDate;

    private Date endDate;
    private Habit habit;

    private RealmList<Log> logs;

    private RealmList<Reminder> reminders;
    private RealmList<Tag> tags;
    private Integer difficulty;

    private Date completedAt;
    private Integer completedAtMinute;

    private Date actualStart;

    private String source;

    private Boolean needsSyncWithRemote;
    private Boolean isRemoteObject;

    private SourceMapping sourceMapping;

    public Quest() {
    }

    public void setDuration(int duration) {
        this.duration = (int) Math.min(TimeUnit.HOURS.toMinutes(Constants.MAX_QUEST_DURATION_HOURS), duration);
    }

    public void setLogs(RealmList<Log> logs) {
        this.logs = logs;
    }

    public RealmList<Reminder> getReminders() {
        return reminders;
    }

    public void setReminders(RealmList<Reminder> reminders) {
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

    public Habit getHabit() {
        return habit;
    }

    public void setHabit(Habit habit) {
        this.habit = habit;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
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

    public int getDifficulty() {
        return difficulty;
    }

    public Quest(String name) {
        this(name, null);
    }

    public Quest(String name, Date endDate) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        setEndDate(endDate);
        this.setStartMinute(null);
        this.createdAt = DateUtils.nowUTC();
        this.updatedAt = DateUtils.nowUTC();
        this.context = QuestContext.PERSONAL.name();
        this.flexibleStartTime = false;
        this.needsSyncWithRemote = true;
        this.isRemoteObject = false;
        this.source = Constants.API_RESOURCE_SOURCE;
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

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = DateUtils.getDate(endDate);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContext() {
        return TextUtils.isEmpty(context) ? QuestContext.PERSONAL.name() : context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public static QuestContext getContext(Quest quest) {
        return QuestContext.valueOf(quest.getContext());
    }

    public static void setContext(Quest quest, QuestContext context) {
        quest.setContext(context.name());
    }

    public static Date getStartDateTime(Quest quest) {
        if (quest.getStartMinute() < 0 || quest.getEndDate() == null) {
            return null;
        }
        Time startTime = Time.of(quest.getStartMinute());
        return new LocalDate(quest.getEndDate(), DateTimeZone.UTC).toDateTime(new LocalTime(startTime.getHours(), startTime.getMinutes())).toDate();
    }

    public Date getActualStart() {
        return actualStart;
    }

    public void setActualStart(Date actualStart) {
        this.actualStart = actualStart;
    }

    public Integer getCompletedAtMinute() {
        return completedAtMinute;
    }

    public void setCompletedAtMinute(Integer completedAtMinute) {
        this.completedAtMinute = completedAtMinute;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        if (completedAt == null) {
            this.completedAt = null;
        } else {
            DateTimeZone tz = DateTimeZone.getDefault();
            this.completedAt = new Date(tz.convertLocalToUTC(completedAt.getTime(), false));
        }
    }

    public static boolean isStarted(Quest quest) {
        return quest.getActualStart() != null && quest.getCompletedAt() == null;
    }

    public static boolean isCompleted(Quest quest) {
        return quest.getCompletedAt() != null;
    }

    public static void setStartTime(Quest quest, Time time) {
        if (time != null) {
            quest.setStartMinute(time.toMinutesAfterMidnight());
        } else {
            quest.setStartMinute(null);
        }
    }

    public boolean isScheduledForToday() {
        return isScheduledFor(new LocalDate());
    }

    public boolean isScheduledFor(LocalDate date) {
        return date.isEqual(new LocalDate(getEndDate(), DateTimeZone.UTC));
    }

    public int getStartMinute() {
        return startMinute != null ? startMinute : -1;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public RealmList<Tag> getTags() {
        return tags;
    }

    public void setTags(RealmList<Tag> tags) {
        this.tags = tags;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

    @Override
    public void setRemoteObject() {
        isRemoteObject = true;
    }

    @Override
    public boolean isRemoteObject() {
        return isRemoteObject;
    }

    public boolean isStarted() {
        return actualStart != null && completedAt == null;
    }

    public boolean isScheduledForTomorrow() {
        return DateUtils.isTomorrowUTC(new LocalDate(getEndDate(), DateTimeZone.UTC).toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate());
    }

    public boolean isIndicator() {
        boolean isCompleted = getCompletedAt() != null;
        boolean repeatsPerDay = getHabit() != null && !TextUtils.isEmpty(getHabit().getRecurrence().getDailyRrule());
        boolean hasShortOrNoDuration = getDuration() < 15;
        return isCompleted && repeatsPerDay && hasShortOrNoDuration;
    }

    public boolean isHabit() {
        return getHabit() != null;
    }

    public SourceMapping getSourceMapping() {
        return sourceMapping;
    }

    public void setSourceMapping(SourceMapping sourceMapping) {
        this.sourceMapping = sourceMapping;
    }
}
