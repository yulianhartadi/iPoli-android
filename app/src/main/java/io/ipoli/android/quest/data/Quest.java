package io.ipoli.android.quest.data;

import android.text.TextUtils;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.net.RemoteObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IDGenerator;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;
import io.ipoli.android.quest.generators.RewardProvider;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class Quest extends RealmObject implements RemoteObject<Quest>, RewardProvider {

    public static final int PRIORITY_MOST_IMPORTANT_FOR_DAY = 4;
    public static final int DEFAULT_NO_PRIORITY_VALUE = -1;

    @Required
    @PrimaryKey
    private String id;

    private String rawText;

    @Required
    private String name;

    private String category;

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
    private Date originalStartDate;

    private Date endDate;
    private RepeatingQuest repeatingQuest;

    private RealmList<Log> logs;

    private RealmList<Reminder> reminders;
    private RealmList<Tag> tags;
    private Integer difficulty;

    private Date completedAt;
    private Integer completedAtMinute;

    private Date actualStart;

    private Challenge challenge;

    private Long coins;
    private Long experience;

    private String note;

    private String source;

    private Boolean needsSyncWithRemote;
    private String remoteId;

    private SourceMapping sourceMapping;
    private boolean isDeleted;

    @Ignore
    private boolean isPlaceholder;

    public Quest() {
    }

    public Quest(String name) {
        this(name, null);
    }

    public Quest(String name, Date endDate) {
        this.id = IDGenerator.generate();
        this.name = name;
        this.reminders = new RealmList<>();
        setEndDateFromLocal(endDate);
        setStartDateFromLocal(endDate);
        this.originalStartDate = DateUtils.getDate(endDate);
        this.setStartMinute(null);
        this.createdAt = DateUtils.nowUTC();
        this.updatedAt = DateUtils.nowUTC();
        this.category = Category.PERSONAL.name();
        this.flexibleStartTime = false;
        this.needsSyncWithRemote = true;
        this.experience = new ExperienceRewardGenerator().generate(this);
        this.coins = new CoinsRewardGenerator().generate(this);
        this.source = Constants.API_RESOURCE_SOURCE;
        this.isDeleted = false;
    }

    public void setDuration(Integer duration) {
        if (duration == null) {
            this.duration = null;
            return;
        }
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
        updateRemindersStartTime();
    }

    public void updateRemindersStartTime() {
        if(getReminders() == null) {
            return;
        }
        for(Reminder r : getReminders()) {
            r.calculateStartTime(this);
        }
    }

    public RepeatingQuest getRepeatingQuest() {
        return repeatingQuest;
    }

    public void setRepeatingQuest(RepeatingQuest repeatingQuest) {
        this.repeatingQuest = repeatingQuest;
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

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDateFromLocal(Date startDate) {
        setStartDate(DateUtils.getDate(startDate));
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

    public void setEndDateFromLocal(Date endDate) {
        setEndDate(DateUtils.getDate(endDate));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return TextUtils.isEmpty(category) ? Category.PERSONAL.name() : category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public static Category getCategory(Quest quest) {
        return Category.valueOf(quest.getCategory());
    }

    public static void setCategory(Quest quest, Category category) {
        quest.setCategory(category.name());
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
        this.completedAt = completedAt;
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

    public boolean isScheduledForThePast() {
        return getEndDate() != null && getEndDate().before(DateUtils.toStartOfDayUTC(LocalDate.now()));
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

    public boolean isStarted() {
        return actualStart != null && completedAt == null;
    }

    public boolean isScheduledForTomorrow() {
        return DateUtils.isTomorrowUTC(DateUtils.toStartOfDayUTC(new LocalDate(getEndDate(), DateTimeZone.UTC)));
    }

    public boolean isIndicator() {
        boolean isCompleted = getCompletedAt() != null;
        return isCompleted && repeatPerDayWithShortOrNoDuration();
    }

    public boolean repeatPerDayWithShortOrNoDuration() {
        boolean repeatsPerDay = getRepeatingQuest() != null && getRepeatingQuest().getRecurrence().getTimesPerDay() > 1;
        boolean hasShortOrNoDuration = getDuration() < Constants.CALENDAR_EVENT_MIN_DURATION;
        return repeatsPerDay && hasShortOrNoDuration;
    }


    public boolean isRepeatingQuest() {
        return getRepeatingQuest() != null;
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

    public void setCoins(long coins) {
        this.coins = coins;
    }

    public void setExperience(long experience) {
        this.experience = experience;
    }

    @Override
    public String getRemoteId() {
        return remoteId;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    public void markDeleted() {
        if (getReminders() != null) {
            for (Reminder r : getReminders()) {
                r.markDeleted();
            }
        }
        isDeleted = true;
        markUpdated();
    }

    @Override
    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public void setFlexibleStartTime(boolean flexibleStartTime) {
        this.flexibleStartTime = flexibleStartTime;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
        updateRemindersStartTime();
    }

    public void setOriginalStartDate(Date originalStartDate) {
        this.originalStartDate = originalStartDate;
    }

    public boolean isPlaceholder() {
        return isPlaceholder;
    }

    public void setPlaceholder(boolean placeholder) {
        isPlaceholder = placeholder;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }
}
