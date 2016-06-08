package io.ipoli.android.quest.data;

import java.util.Date;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IDGenerator;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/8/16.
 */
public class Recurrence extends RealmObject {

    public enum RecurrenceType {WEEKLY, MONTHLY;}

    @Required
    @PrimaryKey
    private String id;

    private int timesPerDay;

    private String rrule;
    private String rdate;
    private String exrule;
    private String exdate;
    private Date dtstart;
    private Date dtend;
    private String type;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    public Recurrence() {

    }

    public Recurrence(int timesPerDay) {
        id = IDGenerator.generate();
        createdAt = DateUtils.nowUTC();
        updatedAt = DateUtils.nowUTC();
        this.timesPerDay = timesPerDay;
    }

    public static Recurrence create() {
        return new Recurrence(1);
    }

    public String getType() {
        return type;
    }

    public int getTimesPerDay() {
        return timesPerDay;
    }

    public String getRrule() {
        return rrule;
    }

    public void setRrule(String rrule, RecurrenceType recurrenceType) {
        this.rrule = rrule;
        this.type = recurrenceType.name();
    }

    public String getRdate() {
        return rdate;
    }

    public void setRdate(String rdate) {
        this.rdate = rdate;
    }

    public String getExrule() {
        return exrule;
    }

    public void setExrule(String exrule) {
        this.exrule = exrule;
    }

    public String getExdate() {
        return exdate;
    }

    public void setExdate(String exdate) {
        this.exdate = exdate;
    }

    public Date getDtstart() {
        return dtstart;
    }

    public void setDtstart(Date dtstart) {
        this.dtstart = dtstart;
    }

    public Date getDtend() {
        return dtend;
    }

    public void setDtend(Date dtend) {
        this.dtend = dtend;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RecurrenceType getRecurrenceType() {
        return RecurrenceType.valueOf(type);
    }
}
