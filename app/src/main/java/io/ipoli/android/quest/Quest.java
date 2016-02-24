package io.ipoli.android.quest;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class Quest extends RealmObject {

    @PrimaryKey
    private String id;

    @Required
    private String name;

    @Required
    private Date createdAt;

    @Required
    private String context;

    private Date due;

    private int duration;
    private Date startTime;
    private Date actualStartDateTime;
    private Date completedAtDateTime;
    private String log;
    private String difficulty;
    private int measuredDuration;

    public Quest() {
    }

    public void setDuration(int duration) {
        this.duration = (int) Math.min(TimeUnit.HOURS.toMinutes(Constants.MAX_QUEST_DURATION_HOURS), duration);
    }

    public int getDuration() {
        return duration;
    }

    public void setStartTime(Date startTime) {
        this.startTime = DateUtils.getNormalizedStartTime(startTime);
    }

    public static Time getStartTime(Quest quest) {
        return Time.of(quest.getStartTime());
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getLog() {
        return log;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Quest(String name) {
        this(name, null);
    }

    public Quest(String name, Date due) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        setDue(due);
        this.createdAt = new Date();
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

    public Date getDue() {
        return due;
    }

    public void setDue(Date due) {
        this.due = DateUtils.getNormalizedDueDate(due);
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

    public static Difficulty getDifficulty(Quest quest) {
        try {
            return Difficulty.valueOf(quest.getDifficulty());
        } catch (Exception e) {
            return Difficulty.UNKNOWN;
        }
    }

    public static QuestContext getContext(Quest quest) {
        return QuestContext.valueOf(quest.getContext());
    }

    public static void setContext(Quest quest, QuestContext context) {
        quest.setContext(context.name());
    }

    public static Date getStartDateTime(Quest quest) {
        Calendar startTime = Calendar.getInstance();
        startTime.setTime(quest.getStartTime());

        Calendar startDateTime = Calendar.getInstance();
        startDateTime.setTime(quest.getDue());
        startDateTime.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY));
        startDateTime.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE));
        return startDateTime.getTime();
    }

    public Date getActualStartDateTime() {
        return actualStartDateTime;
    }

    public void setActualStartDateTime(Date actualStartDateTime) {
        this.actualStartDateTime = actualStartDateTime;
    }

    public int getMeasuredDuration() {
        return measuredDuration;
    }

    public void setMeasuredDuration(int measuredDuration) {
        this.measuredDuration = measuredDuration;
    }

    public Date getCompletedAtDateTime() {
        return completedAtDateTime;
    }

    public void setCompletedAtDateTime(Date completedAtDateTime) {
        this.completedAtDateTime = completedAtDateTime;
    }

    public static boolean isUnplanned(Quest quest) {
        return quest.getDue() == null && quest.getActualStartDateTime() == null && quest.getCompletedAtDateTime() == null;
    }

    public static boolean isPlanned(Quest quest) {
        return quest.getDue() != null && quest.getActualStartDateTime() == null && quest.getCompletedAtDateTime() == null;
    }

    public static boolean isStarted(Quest quest) {
        return quest.getActualStartDateTime() != null && quest.getCompletedAtDateTime() == null;
    }

    public static boolean isCompleted(Quest quest) {
        return quest.getCompletedAtDateTime() != null;
    }

    public static void setStartTime(Quest quest, Time time) {
        quest.setStartTime(time.toDate());
    }

    public Date getStartTime() {
        return startTime;
    }
}
