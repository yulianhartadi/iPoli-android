package io.ipoli.android.quest.data;

import android.text.TextUtils;

import java.util.Date;
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
 * on 3/26/16.
 */
public class RecurrentQuest extends RealmObject implements RemoteObject<RecurrentQuest> {

    @PrimaryKey
    private String id;
    private String rawText;

    private String name;

    private String context;

    private boolean isAllDay;

    private Integer priority;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    private Integer startMinute;

    private Integer duration;
    private RealmList<Reminder> reminders;

    private Recurrence recurrence;

    public RecurrentQuest() {
    }

    public void setDuration(int duration) {
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

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public static Time getStartTime(RecurrentQuest quest) {
        if(quest.getStartMinute() < 0) {
            return null;
        }
        return Time.of(quest.getStartMinute());
    }

    public RecurrentQuest(String rawText) {
        this.id = UUID.randomUUID().toString();
        this.rawText = rawText;
        this.createdAt = DateUtils.nowUTC();
        this.updatedAt = DateUtils.nowUTC();
        this.context = QuestContext.PERSONAL.name();
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
        return TextUtils.isEmpty(context) ? QuestContext.PERSONAL.name() : context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public static QuestContext getContext(RecurrentQuest quest) {
        return QuestContext.valueOf(quest.getContext());
    }

    public static void setContext(RecurrentQuest quest, QuestContext context) {
        quest.setContext(context.name());
    }

    public static void setStartTime(RecurrentQuest quest, Time time) {
        quest.setStartMinute(time.toMinutesAfterMidnight());
    }

    public int getStartMinute() {
        return startMinute != null ? startMinute : -1;
    }

    @Override
    public void markUpdated() {
        updatedAt = DateUtils.nowUTC();
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
}