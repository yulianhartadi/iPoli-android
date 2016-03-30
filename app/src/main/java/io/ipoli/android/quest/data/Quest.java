package io.ipoli.android.quest.data;

import java.util.Calendar;
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

    @PrimaryKey
    private String id;

    private String rawText;

    @Required
    private String name;

    @Required
    private String context;

    private boolean isAllDay;

    private int priority;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    private Date completedAtDateTime;

    private int startMinute;
    private int duration;

    private Date startDate;
    private Date endDate;

    private RecurrentQuest recurrentQuest;

    private RealmList<Log> logs;
    private RealmList<Reminder> reminders;
    private RealmList<Tag> tags;

    private int difficulty;
    private int actualDuration;
    private Date actualStartDateTime;

    private String remoteId;
    private boolean needsSyncWithRemote;

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
        return duration;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public RecurrentQuest getRecurrentQuest() {
        return recurrentQuest;
    }

    public void setRecurrentQuest(RecurrentQuest recurrentQuest) {
        this.recurrentQuest = recurrentQuest;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
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
        return Time.fromMinutesAfterMidnight(quest.getStartMinute());
    }

    public void setDifficulty(int difficulty) {
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
        this.setStartMinute(-1);
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.context = QuestContext.PERSONAL.name();
        this.needsSyncWithRemote = true;
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
        this.endDate = DateUtils.getNormalizedDueDate(endDate);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContext() {
        return context;
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
        Calendar startDateTime = Calendar.getInstance();
        startDateTime.setTime(quest.getEndDate());
        int h = (int) TimeUnit.MINUTES.toHours(quest.getStartMinute());
        int min = quest.getStartMinute() - h * 60;
        startDateTime.set(Calendar.HOUR_OF_DAY, h);
        startDateTime.set(Calendar.MINUTE, min);
        return startDateTime.getTime();
    }

    public Date getActualStartDateTime() {
        return actualStartDateTime;
    }

    public void setActualStartDateTime(Date actualStartDateTime) {
        this.actualStartDateTime = actualStartDateTime;
    }

    public int getActualDuration() {
        return actualDuration;
    }

    public void setActualDuration(int actualDuration) {
        this.actualDuration = actualDuration;
    }

    public Date getCompletedAtDateTime() {
        return completedAtDateTime;
    }

    public void setCompletedAtDateTime(Date completedAtDateTime) {
        this.completedAtDateTime = completedAtDateTime;
    }

    public static boolean isUnplanned(Quest quest) {
        return quest.getEndDate() == null && quest.getActualStartDateTime() == null && quest.getCompletedAtDateTime() == null;
    }

    public static boolean isPlanned(Quest quest) {
        return quest.getEndDate() != null && quest.getActualStartDateTime() == null && quest.getCompletedAtDateTime() == null;
    }

    public static boolean isStarted(Quest quest) {
        return quest.getActualStartDateTime() != null && quest.getCompletedAtDateTime() == null;
    }

    public static boolean isCompleted(Quest quest) {
        return quest.getCompletedAtDateTime() != null;
    }

    public static void setStartTime(Quest quest, Time time) {
        quest.setStartMinute(time.toMinutesAfterMidnight());
    }

    public int getStartMinute() {
        return startMinute;
    }

    @Override
    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    @Override
    public String getRemoteId() {
        return remoteId;
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
        this.needsSyncWithRemote = false;
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

    @Override
    public void markUpdated() {
        setNeedsSync();
        setUpdatedAt(new Date());
    }
}
