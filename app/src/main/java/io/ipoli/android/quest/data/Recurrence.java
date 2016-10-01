package io.ipoli.android.quest.data;

import com.google.firebase.database.Exclude;

import org.joda.time.LocalDate;

import java.util.Date;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/8/16.
 */
public class Recurrence extends PersistedObject {

    public static final String RRULE_EVERY_DAY = "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR,SA,SU";

    public enum RecurrenceType {DAILY, WEEKLY, MONTHLY;}

    private int timesADay;
    private int flexibleCount;

    private String rrule;

    private String rdate;

    private String exrule;
    private String exdate;
    private Long dtstart;
    private Long dtend;
    private String type;

    public Recurrence() {

    }

    public Recurrence(int timesADay) {
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
        setRecurrenceType(RecurrenceType.DAILY);
        setDtstartDate(DateUtils.toStartOfDay(LocalDate.now()));
        this.timesADay = timesADay;
        this.flexibleCount = 0;
    }

    public void setTimesADay(int timesADay) {
        this.timesADay = timesADay;
    }

    public static Recurrence create() {
        return new Recurrence(1);
    }

    @Exclude
    public void setRecurrenceType(RecurrenceType type) {
        this.type = type.name();
    }

    public void setType(String type) {
        this.type = type;
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

    @Exclude
    public Date getDtstartDate() {
        return dtstart != null ? new Date(dtstart) : null;
    }

    @Exclude
    public void setDtstartDate(Date dtstartDate) {
        dtstart = dtstartDate != null ? dtstartDate.getTime() : null;
    }

    public Long getDtstart() {
        return dtstart;
    }

    public void setDtstart(Long dtstart) {
        this.dtstart = dtstart;
    }

    @Exclude
    public Date getDtendDate() {
        return dtend != null ? new Date(dtend) : null;
    }

    @Exclude
    public void setDtendDate(Date dtendDate) {
        dtend = dtendDate != null ? dtendDate.getTime() : null;
    }

    public Long getDtend() {
        return dtend;
    }

    public void setDtend(Long dtend) {
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
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public int getFlexibleCount() {
        return flexibleCount;
    }

    public void setFlexibleCount(int flexibleCount) {
        this.flexibleCount = flexibleCount;
    }

    @Exclude
    public RecurrenceType getRecurrenceType() {
        return RecurrenceType.valueOf(type);
    }

    @Exclude
    public boolean isFlexible() {
        return flexibleCount > 0;
    }
}
