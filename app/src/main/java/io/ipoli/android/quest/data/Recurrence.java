package io.ipoli.android.quest.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.threeten.bp.LocalDate;

import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/8/16.
 */
public class Recurrence {

    public static final String RRULE_EVERY_DAY = "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR,SA,SU";

    public enum RepeatType {DAILY, WEEKLY, MONTHLY, YEARLY;}

    private int flexibleCount;

    private String rrule;

    private String rdate;

    private String exrule;
    private String exdate;
    private Long dtstart;
    private Long dtend;
    private String repeatType;

    public Recurrence() {

    }

    public static Recurrence create() {
        Recurrence recurrence = new Recurrence();
        recurrence.setRecurrenceType(RepeatType.DAILY);
        recurrence.setDtstartDate(LocalDate.now());
        recurrence.setFlexibleCount(0);
        return recurrence;
    }

    @JsonIgnore
    public void setRecurrenceType(RepeatType type) {
        this.repeatType = type.name();
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public String getRepeatType() {
        return repeatType;
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

    @JsonIgnore
    public LocalDate getDtstartDate() {
        return dtstart != null ? DateUtils.fromMillis(dtstart) : null;
    }

    @JsonIgnore
    public void setDtstartDate(LocalDate dtstartDate) {
        dtstart = dtstartDate != null ? DateUtils.toMillis(dtstartDate) : null;
    }

    public Long getDtstart() {
        return dtstart;
    }

    public void setDtstart(Long dtstart) {
        this.dtstart = dtstart;
    }

    @JsonIgnore
    public LocalDate getDtendDate() {
        return dtend != null ? DateUtils.fromMillis(dtend) : null;
    }

    @JsonIgnore
    public void setDtendDate(LocalDate dtendDate) {
        dtend = dtendDate != null ? DateUtils.toMillis(dtendDate) : null;
    }

    public Long getDtend() {
        return dtend;
    }

    public void setDtend(Long dtend) {
        this.dtend = dtend;
    }

    public int getFlexibleCount() {
        return flexibleCount;
    }

    public void setFlexibleCount(int flexibleCount) {
        this.flexibleCount = flexibleCount;
    }

    @JsonIgnore
    public RepeatType getRecurrenceType() {
        return RepeatType.valueOf(repeatType);
    }

    @JsonIgnore
    public boolean isFlexible() {
        return flexibleCount > 0;
    }
}
