package io.ipoli.android.quest.data;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.net.RemoteObject;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IDGenerator;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestContext;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/26/16.
 */
public class RepeatingQuest extends RealmObject implements RemoteObject<RepeatingQuest> {

    @Required
    @PrimaryKey
    private String id;
    private String rawText;

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
    private RealmList<Reminder> reminders;

    private Recurrence recurrence;

    private String source;

    private boolean needsSyncWithRemote;
    private String remoteId;

    private SourceMapping sourceMapping;
    private boolean isDeleted;

    public RepeatingQuest() {
    }

    public void setDuration(Integer duration) {
        this.duration = (int) Math.min(TimeUnit.HOURS.toMinutes(Constants.MAX_QUEST_DURATION_HOURS), duration);
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

    public static Time getStartTime(RepeatingQuest quest) {
        if (quest.getStartMinute() < 0) {
            return null;
        }
        return Time.of(quest.getStartMinute());
    }

    public RepeatingQuest(String rawText) {
        this.id = IDGenerator.generate();
        this.rawText = rawText;
        this.createdAt = DateUtils.nowUTC();
        this.updatedAt = DateUtils.nowUTC();
        this.context = QuestContext.PERSONAL.name();
        this.flexibleStartTime = false;
        this.needsSyncWithRemote = true;
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

    public String getContext() {
        return context == null || context.isEmpty() ? QuestContext.PERSONAL.name() : context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public static QuestContext getContext(RepeatingQuest quest) {
        return QuestContext.valueOf(quest.getContext());
    }

    public static void setContext(RepeatingQuest quest, QuestContext context) {
        quest.setContext(context.name());
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

    @Override
    public void markUpdated() {
        setNeedsSync();
        setUpdatedAt(DateUtils.nowUTC());
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
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

    @Override
    public String getRemoteId() {
        return remoteId;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public void markDeleted() {
        isDeleted = true;
        markUpdated();
    }

    public boolean isWeekly() {
        return getRecurrence().getRecurrenceType() == Recurrence.RecurrenceType.WEEKLY;
    }

    public boolean isMonthly() {
        return getRecurrence().getRecurrenceType() == Recurrence.RecurrenceType.MONTHLY;
    }

    @Override
    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }
}