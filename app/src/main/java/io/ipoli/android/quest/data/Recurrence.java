package io.ipoli.android.quest.data;

import java.util.Date;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/8/16.
 */
public class Recurrence extends PersistedObject {

    public enum RecurrenceType {DAILY, WEEKLY, MONTHLY;}

    private int timesADay;
    private int flexibleCount;

    private String rrule;

    private String rdate;

    private String exrule;
    private String exdate;
    private Date dtstart;
    private Date dtend;
    private String type;

    public Recurrence() {

    }

    public Recurrence(int timesADay) {
        createdAt = DateUtils.nowUTC();
        updatedAt = DateUtils.nowUTC();
        setType(RecurrenceType.DAILY);
        this.timesADay = timesADay;
        this.flexibleCount = 0;
    }

    public void setTimesADay(int timesADay) {
        this.timesADay = timesADay;
    }

    public static Recurrence create() {
        return new Recurrence(1);
    }

    public void setType(RecurrenceType type) {
        this.type = type.name();
    }

    public String getType() {
        return type;
    }

    public int getTimesADay() {
        return timesADay;
    }

    public String getRrule() {
        return rrule;
    }

    public void setRrule(String rrule) {
        this.rrule = rrule;
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

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public void setIsDeleted(boolean deleted) {
        this.isDeleted = deleted;
    }

    @Override
    public boolean getIsDeleted() {
        return isDeleted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public int getFlexibleCount() {
        return flexibleCount;
    }

    public void setFlexibleCount(int flexibleCount) {
        this.flexibleCount = flexibleCount;
    }

    public RecurrenceType getRecurrenceType() {
        return RecurrenceType.valueOf(type);
    }

    public boolean isFlexible() {
        return flexibleCount > 0;
    }
}
