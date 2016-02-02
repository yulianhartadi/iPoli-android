package io.ipoli.android.quest;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import io.ipoli.android.app.utils.DateUtils;
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
    private String status;

    private Date due;

    private int duration;
    private Date startTime;
    private Date actualStartDateTime;
    private String log;
    private String difficulty;

    public Quest() {
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setStartTime(Date startTime) {
        this.startTime = DateUtils.getNormalizedStartTime(startTime);
    }

    public Date getStartTime() {
        return startTime;
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
        this(name, Status.UNPLANNED.name(), null);
    }

    public Quest(String name, String status, Date due) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.status = status;
        this.due = due;
        this.createdAt = new Date();
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
        this.due = due;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static Difficulty getDifficulty(Quest quest) {
        try {
            return Difficulty.valueOf(quest.getDifficulty());
        } catch (Exception e) {
            return Difficulty.UNKNOWN;
        }
    }

    public static Status getStatus(Quest quest) {
        return Status.valueOf(quest.getStatus());
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
}
